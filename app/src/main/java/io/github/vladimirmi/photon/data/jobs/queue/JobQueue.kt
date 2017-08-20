package io.github.vladimirmi.photon.data.jobs.queue

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.JobManager
import com.birbit.android.jobqueue.persistentQueue.sqlite.SqliteJobQueue
import io.github.vladimirmi.photon.data.jobs.PhotocardAddToFavoriteJob
import io.github.vladimirmi.photon.data.jobs.PhotocardDeleteFromFavoriteJob
import io.github.vladimirmi.photon.data.jobs.queue.JobTask.Type.*
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.realm.Task
import io.github.vladimirmi.photon.data.models.realm.deleteAll
import io.github.vladimirmi.photon.data.models.realm.find
import io.github.vladimirmi.photon.utils.JobStatus
import io.github.vladimirmi.photon.utils.ioToMain
import io.github.vladimirmi.photon.utils.observableFor
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Created by Vladimir Mikhalev 16.08.2017.
 */

class JobQueue(private val dataManager: DataManager,
               private val jobManager: JobManager) {

    private val javaSerializer = SqliteJobQueue.JavaSerializer()

    private val root = dataManager.getDetachedObjFromDb(Task::class.java, "ROOT") ?:
            Task(id = "ROOT", entityId = "ROOT", type = CREATE.name)
                    .apply { dataManager.saveToDB(this) }


    fun add(job: JobTask): Observable<JobStatus> {
        with(job) {
            when (type) {
                CREATE -> getParentForCreateJob(job)
                UNIQUE -> getParentForUniqueJob(job)
                DELETE -> getParentForDeleteJob(job)
                NORMAL -> getParentForNormalJob(job)
                OTHER -> getParentForOtherJob(job)
            }?.queue?.add(createTask(job))
            dataManager.saveToDB(root)
        }
        return jobManager.observableFor(job as Job)
    }

    private fun createTask(job: JobTask) = with(job) {
        Task(id = (job as Job).id,
                entityId = entityId,
                tag = tag,
                type = type.name,
                task = javaSerializer.serialize(job))
    }


    fun execQueue(): Observable<Unit> {
        return dataManager.isNetworkAvailable()
                .filter { it }
                .flatMap { dataManager.getObjectFromDb(Task::class.java, "ROOT") }
                .filter { it.queue.isNotEmpty() }
                .flatMapSingle { execTask() }
                .ioToMain()
    }

    private fun execTask(): Single<Unit> {
        val task = root.queue[0]
        val job = javaSerializer.deserialize<Job>(task.task)
        (job as? JobTask)?.apply { parentEntityId = task.parentEntityId }
                ?: throw IllegalStateException("Job${job.tags} not implements JobTask interface")

        return Single.just(jobManager.addJobInBackground(job))
                .flatMap {
                    job as JobTask
                    if (job.type == CREATE) {
                        jobManager.observableFor(job)
                                .filter { it.status == JobStatus.Status.DONE }
                                .cast(JobTask::class.java)
                                .map { it.entityId }
                                .first("")
                    } else Single.just("")
                }
                .map { newId ->
                    if (newId.isNotEmpty()) {
                        task.queue.forEach { it.parentEntityId = newId }
                    }
                    root.queue.addAll(task.queue)
                    root.queue.remove(task)
                    dataManager.saveToDB(root)
                }
    }

    private fun getParentForCreateJob(job: JobTask) =
            root.find { it.entityId == job.parentEntityId && it.type == CREATE.name }


    private fun getParentForUniqueJob(job: JobTask): Task {
        root.deleteAll { it.entityId == job.entityId && it.tag == job.tag }

        return root.find { it.entityId == job.entityId && it.type == CREATE.name }
    }

    private fun getParentForOtherJob(job: JobTask): Task? {
        return when (job) {
            is PhotocardDeleteFromFavoriteJob -> {
                val deleted = root.deleteAll {
                    it.entityId == job.entityId && it.tag == PhotocardAddToFavoriteJob.TAG
                }
                return if (deleted.isNotEmpty()) null
                else root.find { it.entityId == job.entityId && it.type == CREATE.name }
            }
            else -> root
        }
    }

    private fun getParentForNormalJob(job: JobTask) =
            root.find { it.entityId == job.entityId && it.type == CREATE.name }

    private fun getParentForDeleteJob(job: JobTask): Task? {
        val deleted = root.deleteAll { it.entityId == job.entityId }
        return if (deleted.find { it.type == CREATE.name } != null) root else null
    }

}

package io.github.vladimirmi.photon.data.jobs.queue

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.JobManager
import io.github.vladimirmi.photon.data.jobs.*
import io.github.vladimirmi.photon.data.jobs.queue.JobTask.Type.*
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.realm.Task
import io.github.vladimirmi.photon.data.models.realm.deleteAll
import io.github.vladimirmi.photon.data.models.realm.findParent
import io.github.vladimirmi.photon.utils.JobStatus
import io.github.vladimirmi.photon.utils.Serializer
import io.github.vladimirmi.photon.utils.observableFor
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Created by Vladimir Mikhalev 16.08.2017.
 */

class JobQueue(private val dataManager: DataManager,
               private val jobManager: JobManager) {

    private val serializer = Serializer()

    private val root = dataManager.getDetachedObjFromDb(Task::class.java, "ROOT") ?:
            Task(id = "ROOT", entityId = "ROOT", type = CREATE.name)
                    .apply { dataManager.saveToDB(this) }


    fun add(job: JobTask): Observable<JobStatus> {
        job.onQueued()
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

    fun execQueue(): Observable<Unit> {
        return dataManager.isNetworkAvailable()
                .filter { it }
                .flatMap { dataManager.getObjectFromDb(Task::class.java, "ROOT") }
                .filter { it.queue.isNotEmpty() }
                .flatMapSingle { execTask() }
    }

    private fun execTask(): Single<Unit> {
        val task = root.queue[0]
        val job = createJob(task)
//        (job as? JobTask)?.apply { parentEntityId = task.parentEntityId }
//                ?: throw IllegalStateException("Job${job.tags} not implements JobTask interface")

        return Single.just(jobManager.addJobInBackground(job))
                .flatMap {
                    if ((job as JobTask).type == CREATE) {
                        jobManager.observableFor(job)
                                .filter { it.status == JobStatus.Status.DONE }
                                .map { (it.job as JobTask).entityId }
                                .first("")
                    } else Single.just("")
                }
                .map { newId ->
                    if (newId.isNotEmpty()) {
                        task.queue.forEach { it.parentEntityId = newId }
                    }
                    root.queue.remove(task)
                    dataManager.removeFromDb(Task::class.java, task.id)
                    root.queue.addAll(task.queue)
                    dataManager.saveToDB(root)
                }
    }


    private fun createTask(job: JobTask) = with(job) {
        Task(id = (job as Job).id,
                entityId = entityId,
                tag = tag,
                type = type.name,
                request = serializer.serialize(request))
    }

    private fun createJob(task: Task): Job = with(task) {
        when (tag) {
            AlbumCreateJob.TAG -> AlbumCreateJob(serializer.deserialize(request!!))
            AlbumDeleteJob.TAG -> AlbumDeleteJob(entityId)
            AlbumEditJob.TAG -> AlbumEditJob(serializer.deserialize(request!!))
            PhotocardCreateJob.TAG -> PhotocardCreateJob(entityId, parentEntityId)
            PhotocardDeleteJob.TAG -> PhotocardDeleteJob(entityId)
            PhotocardAddViewJob.TAG -> PhotocardAddViewJob(entityId)
            PhotocardAddToFavoriteJob.TAG -> PhotocardAddToFavoriteJob(entityId)
            PhotocardDeleteFromFavoriteJob.TAG -> PhotocardDeleteFromFavoriteJob(entityId)
            ProfileEditJob.TAG -> ProfileEditJob(serializer.deserialize(request!!))
            else -> throw IllegalArgumentException("Unknown task")
        }
    }

    private fun getParentForCreateJob(job: JobTask) = root.findParent(job.parentEntityId)

    private fun getParentForUniqueJob(job: JobTask): Task {
        val deleted = root.deleteAll { it.entityId == job.entityId && it.tag == job.tag }
        deleted.forEach { dataManager.removeFromDb(Task::class.java, it.id) }
        return root.findParent(job.parentEntityId)
    }

    private fun getParentForOtherJob(job: JobTask): Task? {
        return when (job) {
            is PhotocardDeleteFromFavoriteJob -> {
                val deleted = root.deleteAll {
                    it.entityId == job.entityId && it.tag == PhotocardAddToFavoriteJob.TAG
                }
                deleted.forEach { dataManager.removeFromDb(Task::class.java, it.id) }
                return if (deleted.isNotEmpty()) null
                else root.findParent(job.parentEntityId)
            }
            else -> root
        }
    }

    private fun getParentForNormalJob(job: JobTask) = root.findParent(job.parentEntityId)

    private fun getParentForDeleteJob(job: JobTask): Task? {
        val deleted = root.deleteAll { it.entityId == job.entityId }
        deleted.forEach { dataManager.removeFromDb(Task::class.java, it.id) }
        return if (deleted.find { it.type == CREATE.name } == null) root else null
    }
}

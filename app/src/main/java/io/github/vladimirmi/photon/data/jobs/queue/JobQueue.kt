package io.github.vladimirmi.photon.data.jobs.queue

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.persistentQueue.sqlite.SqliteJobQueue
import io.github.vladimirmi.photon.data.jobs.AlbumCreateJob
import io.github.vladimirmi.photon.data.jobs.PhotocardCreateJob
import io.github.vladimirmi.photon.data.models.realm.Task
import io.github.vladimirmi.photon.data.models.realm.deleteAll
import io.github.vladimirmi.photon.data.models.realm.find
import io.github.vladimirmi.photon.utils.JobType

/**
 * Created by Vladimir Mikhalev 16.08.2017.
 */

class JobQueue(val root: Task) {

    private val javaSerializer = SqliteJobQueue.JavaSerializer()

    fun <T : Job> addJob(entityId: String, tag: String, type: JobType, job: T) {
        val task = Task(entityId = entityId,
                tag = tag,
                type = type.name,
                task = javaSerializer.serialize(job))

        when (type) {
            JobType.CREATE -> handleCreateJob(entityId, tag, job, task)
            JobType.UNIQUE -> handleUniqueJob(entityId, tag, job, task)
            JobType.DELETE -> handleDeleteJob(entityId, tag, job, task)
            else -> return
        }
    }

    private fun <T> handleCreateJob(entityId: String, tag: String, job: T, task: Task) {
        when (tag) {
            AlbumCreateJob.TAG -> root.next.add(task)
            PhotocardCreateJob.TAG -> {
                root.find { it.entityId == (job as PhotocardCreateJob).albumId && it.type == JobType.CREATE.name }?.
                        next?.add(task) ?: root.next.add(task)
            }
        }
    }

    private fun <T> handleUniqueJob(entityId: String, tag: String, job: T, task: Task) {
        root.deleteAll { it.entityId == entityId && it.tag == tag }

        root.find { it.entityId == entityId && it.type == JobType.CREATE.name }?.
                next?.add(task) ?: root.next.add(task)

    }

    private fun <T> handleDeleteJob(entityId: String, tag: String, job: T, task: Task) {
        val deleted = root.deleteAll { it.entityId == entityId }
        deleted.find { it.type == JobType.CREATE.name } ?: root.next.add(task)
    }
}

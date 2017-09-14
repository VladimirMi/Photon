package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.JobManager
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.TagConstraint
import io.github.vladimirmi.photon.data.jobs.album.*
import io.github.vladimirmi.photon.data.jobs.photocard.PhotocardAddViewJob
import io.github.vladimirmi.photon.data.jobs.photocard.PhotocardCreateJob
import io.github.vladimirmi.photon.data.jobs.photocard.PhotocardDeleteJob
import io.github.vladimirmi.photon.data.jobs.profile.ProfileEditJob
import io.github.vladimirmi.photon.data.managers.extensions.JobPriority
import io.github.vladimirmi.photon.data.managers.extensions.cancelOrWaitConnection
import io.github.vladimirmi.photon.data.managers.extensions.logCancel
import io.github.vladimirmi.photon.data.models.realm.isTemp
import io.github.vladimirmi.photon.di.DaggerService

/**
 * Created by Vladimir Mikhalev 10.09.2017.
 */

abstract class ChainJob(private val jobTag: String,
                        private val group: String,
                        private val entityId: String)
    : Job(Params(JobPriority.HIGH)
        .addTags(jobTag + entityId, group + entityId)
        .requireNetwork()
        .persist()) {

    open val needCreate: List<String> = emptyList()
    open val needCancel: String? = null
    open val needReplace: String? = null

    protected val queue = QueueJobHolder(jobTag, group, entityId)

    val tag = jobTag + entityId
    val groupTag = group + entityId
    val newTag get() = jobTag + (result ?: entityId)
    var result: String? = null
    var isChain = false

    fun getJob(): Job? {
        val jobManager = DaggerService.appComponent.jobManager()
        val parent = getParent(needCreate, jobManager)
        if (parent != null) {
            if (parent.tag == needCancel) {
                jobManager.cancelJobs(TagConstraint.ANY, needCancel)
                return null
            } else {
                parent.cancel(needCancel)
            }
            parent.replace(needReplace, QueueJobHolder(jobTag, group, entityId))
            return parent.copy()
        }
        val canceled = jobManager.cancelJobs(TagConstraint.ANY, needCancel)
        if (canceled.cancelledJobs.isNotEmpty()) return null
        jobManager.cancelJobs(TagConstraint.ANY, needReplace)
        return this
    }

    private fun ChainJob.replace(replace: String?, jobHolder: QueueJobHolder) {
        replace?.let { queue.removeFromQueue(it) }
        val parentQueue = queue.findRecurs { it.tag == replace }
        parentQueue?.add(jobHolder)
    }

    private fun ChainJob.cancel(cancel: String?) {
        cancel?.let { queue.removeFromQueue(it) }
    }

    abstract fun copy(): Job

    private fun getParent(needCreate: List<String>, jobManager: JobManager): ChainJob? {
        needCreate.forEach {
            if (!it.isTemp()) return null
            val cancelResult = jobManager.cancelJobs(TagConstraint.ANY, it)
            val parent = cancelResult.cancelledJobs.firstOrNull()
            parent?.let { return it as ChainJob }
        }
        return null
    }

    override final fun onRun() {
        if (isChain && result.isNullOrEmpty()) throw IllegalStateException()
        execute()
        if (queue.isNotEmpty() && result.isNullOrEmpty()) throw IllegalStateException()

        queue.forEach {
            it.result = result
            nextJob(it)
        }
    }

    abstract fun execute()

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int) =
            cancelOrWaitConnection(throwable, runCount)

    override fun onAdded() {}

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        logCancel(cancelReason, throwable)
    }

    private fun nextJob(queueJobHolder: QueueJobHolder) {
        val jobManager = DaggerService.appComponent.jobManager()
        val job = when (queueJobHolder.jobTag) {
            ProfileEditJob.TAG -> ProfileEditJob(queueJobHolder.entityId).apply { setupJob(queueJobHolder) }
            AlbumAddFavoritePhotoJob.TAG -> AlbumAddFavoritePhotoJob(queueJobHolder.entityId).apply { setupJob(queueJobHolder) }
            AlbumCreateJob.TAG -> AlbumCreateJob(queueJobHolder.entityId).apply { setupJob(queueJobHolder) }
            AlbumDeleteFavoritePhotoJob.TAG -> AlbumDeleteFavoritePhotoJob(queueJobHolder.entityId).apply { setupJob(queueJobHolder) }
            AlbumDeleteJob.TAG -> AlbumDeleteJob(queueJobHolder.entityId).apply { setupJob(queueJobHolder) }
            AlbumEditJob.TAG -> AlbumEditJob(queueJobHolder.entityId).apply { setupJob(queueJobHolder) }
            PhotocardAddViewJob.TAG -> PhotocardAddViewJob(queueJobHolder.entityId).apply { setupJob(queueJobHolder) }
            PhotocardCreateJob.TAG -> PhotocardCreateJob(queueJobHolder.entityId).apply { setupJob(queueJobHolder) }
            PhotocardDeleteJob.TAG -> PhotocardDeleteJob(queueJobHolder.entityId).apply { setupJob(queueJobHolder) }
            else -> throw IllegalStateException()
        }
        jobManager.addJob(job)
    }

    private fun ChainJob.setupJob(jobHolder: QueueJobHolder): ChainJob {
        result = jobHolder.result
        queue.addAll(jobHolder)
        isChain = true
        return this
    }
}
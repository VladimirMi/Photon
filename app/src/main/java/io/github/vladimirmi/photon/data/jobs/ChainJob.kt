package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.*
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
import timber.log.Timber

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
                jobManager.cancel(needCancel)
                return null
            } else {
                parent.queue.cancel(needCancel)
            }
            val jobHolder = QueueJobHolder(jobTag, group, entityId)
            parent.queue.replace(needReplace, jobHolder)
            parent.queue.add(needCreate.first(), jobHolder)
            Timber.e("${parent.queue}")
            return parent.copy()
        }
        if (jobManager.cancel(needCancel)?.cancelledJobs?.isNotEmpty() == true) return null
        jobManager.cancel(needReplace)
        return this
    }

    abstract fun copy(): Job

    override final fun onRun() {
        if (isChain && result.isNullOrEmpty()) throw IllegalStateException()
        onStart()
        if (queue.isNotEmpty() && result.isNullOrEmpty()) throw IllegalStateException()

        queue.forEach {
            it.result = result
            nextJob(it)
        }
    }

    abstract fun onStart()

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int) =
            cancelOrWaitConnection(throwable, runCount)

    override fun onAdded() {}

    override final fun onCancel(cancelReason: Int, throwable: Throwable?) {
        logCancel(cancelReason, throwable)
        throwable?.let { onError(it) }
    }

    abstract fun onError(throwable: Throwable)

    private fun getParent(tags: List<String>, jobManager: JobManager): ChainJob? {
        tags.forEach {
            if (!it.isTemp()) return null
            val cancelResult = jobManager.cancelJobs(TagConstraint.ANY, it)
            val parent = cancelResult.cancelledJobs.firstOrNull()
            parent?.let { return it as ChainJob }
        }
        return null
    }

    private fun nextJob(jobHolder: QueueJobHolder) {
        val jobManager = DaggerService.appComponent.jobManager()
        val job = when (jobHolder.jobTag) {
            ProfileEditJob.TAG -> ProfileEditJob(jobHolder.entityId).setupJob(jobHolder)
            AlbumAddFavoritePhotoJob.TAG -> AlbumAddFavoritePhotoJob(jobHolder.entityId).setupJob(jobHolder)
            AlbumCreateJob.TAG -> AlbumCreateJob(jobHolder.entityId).setupJob(jobHolder)
            AlbumDeleteFavoritePhotoJob.TAG -> AlbumDeleteFavoritePhotoJob(jobHolder.entityId).setupJob(jobHolder)
            AlbumDeleteJob.TAG -> AlbumDeleteJob(jobHolder.entityId).setupJob(jobHolder)
            AlbumEditJob.TAG -> AlbumEditJob(jobHolder.entityId).setupJob(jobHolder)
            PhotocardAddViewJob.TAG -> PhotocardAddViewJob(jobHolder.entityId).setupJob(jobHolder)
            PhotocardCreateJob.TAG -> PhotocardCreateJob(jobHolder.entityId).setupJob(jobHolder)
            PhotocardDeleteJob.TAG -> PhotocardDeleteJob(jobHolder.entityId).setupJob(jobHolder)
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

    private fun JobManager.cancel(tag: String?): CancelResult? =
            tag?.let { cancelJobs(TagConstraint.ANY, tag) }
}
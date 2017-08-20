package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.CancelReason
import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import io.github.vladimirmi.photon.data.jobs.queue.JobTask
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.*
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * Created by Vladimir Mikhalev 21.07.2017.
 */

class PhotocardAddToFavoriteJob(photocardId: String)
    : Job(Params(JobPriority.LOW)
        .setGroupId(JobGroup.PHOTOCARD)
        .requireNetwork()
        .persist()), JobTask {

    companion object {
        const val TAG = "PhotocardAddToFavoriteJob"
    }

    override var entityId = photocardId
    override var parentEntityId = photocardId
    override val tag = TAG
    override val type = JobTask.Type.UNIQUE

    override fun onAdded() {}

    override fun onRun() {
        val dataManager = DaggerService.appComponent.dataManager()
        var error: Throwable? = null

        dataManager.addToFavorite(parentEntityId)
                .blockingSubscribe({}, { error = it })

        error?.let { throw it }
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int) =
            cancelOrWait(throwable, runCount)

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        logCancel(cancelReason, throwable)
        if (cancelReason == CancelReason.CANCELLED_VIA_SHOULD_RE_RUN) {
            updateAlbum()
        }
    }

    private fun updateAlbum() {
        val dataManager = DaggerService.appComponent.dataManager()
        val favAlbumId = dataManager.getUserFavAlbumId()

        dataManager.getAlbumFromNet(favAlbumId, Date(0).toString())
                .doOnNext { dataManager.saveToDB(it) }
                .subscribeOn(Schedulers.io())
                .subscribeWith(ErrorObserver())
    }
}
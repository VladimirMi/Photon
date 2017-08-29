package io.github.vladimirmi.photon.data.jobs.album

import com.birbit.android.jobqueue.CancelReason
import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.*
import io.reactivex.schedulers.Schedulers

/**
 * Created by Vladimir Mikhalev 21.07.2017.
 */

class AlbumAddFavoritePhotoJob(private val photocardId: String)
    : Job(Params(JobPriority.LOW)
        .setGroupId(JobGroup.PHOTOCARD)
        .addTags(TAG)
        .requireNetwork()) {

    companion object {
        const val TAG = "AlbumAddFavoritePhotoJob"
    }

    override fun onAdded() {}

    override fun onRun() {
        val dataManager = DaggerService.appComponent.dataManager()
        var error: Throwable? = null

        dataManager.addToFavorite(photocardId)
                .blockingSubscribe({}, { error = it })

        error?.let { throw it }
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int) =
            cancelOrWaitConnection(throwable, runCount)

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        logCancel(cancelReason, throwable)
        if (cancelReason == CancelReason.CANCELLED_VIA_SHOULD_RE_RUN) {
            updateAlbum()
        }
    }

    private fun updateAlbum() {
        val dataManager = DaggerService.appComponent.dataManager()
        val favAlbumId = dataManager.getUserFavAlbumId()

        dataManager.getAlbumFromNet(favAlbumId, "0")
                .doOnNext { dataManager.saveFromNet(it) }
                .subscribeOn(Schedulers.io())
                .subscribeWith(ErrorObserver())
    }
}
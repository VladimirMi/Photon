package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.CancelReason
import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.*
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * Created by Vladimir Mikhalev 21.07.2017.
 */

class PhotocardAddToFavoriteJob(val photocardId: String,
                                val favAlbumId: String,
                                val skipNetworkPart: Boolean = false,
                                val onlyNetworkPart: Boolean = false)
    : Job(Params(JobPriority.LOW)
        .setDelayMs(1000)
        .setGroupId(JobGroup.PHOTOCARD)
        .addTags(TAG + photocardId, JobGroup.PHOTOCARD + photocardId)
        .requireNetwork()
        .persist()) {

    companion object {
        const val TAG = "PhotocardAddToFavoriteJob"
    }

    override fun onAdded() {
        if (onlyNetworkPart) return
        val dataManager = DaggerService.appComponent.dataManager()
        val album = dataManager.getDetachedObjFromDb(Album::class.java, favAlbumId)!!
        val photocard = dataManager.getDetachedObjFromDb(Photocard::class.java, photocardId)!!
        dataManager.saveToDB(album.apply { photocards.add(photocard) })
    }

    override fun onRun() {
        if (skipNetworkPart) return
        val dataManager = DaggerService.appComponent.dataManager()
        var error: Throwable? = null

        dataManager.addToFavorite(photocardId)
                .blockingSubscribe({}, { error = it })

        error?.let { throw it }
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int): RetryConstraint {
        return cancelOrWait(throwable, runCount)
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        logCancel(cancelReason, throwable)
        if (cancelReason == CancelReason.CANCELLED_VIA_SHOULD_RE_RUN) {
            updateAlbum()
        }
    }

    private fun updateAlbum() {
        val dataManager = DaggerService.appComponent.dataManager()

        dataManager.getAlbumFromNet(favAlbumId, Date(0).toString())
                .doOnNext { dataManager.saveToDB(it) }
                .subscribeOn(Schedulers.io())
                .subscribeWith(ErrorObserver())
    }
}
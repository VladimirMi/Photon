package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.CancelReason
import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.AppConfig
import io.github.vladimirmi.photon.utils.ErrorObserver
import io.reactivex.schedulers.Schedulers
import java.net.SocketTimeoutException
import java.util.*

/**
 * Created by Vladimir Mikhalev 21.07.2017.
 */

class AddToFavoriteJob(private val photocardId: String,
                       private val favAlbumId: String,
                       private val skipNetworkPart: Boolean)
    : Job(Params(JobPriority.HIGH)
        .setGroupId(TAG)
        .addTags(TAG + photocardId)
        .requireNetwork()
        .persist()) {

    companion object {
        const val TAG = "AddToFavoriteJob"
    }

    val tag = TAG + photocardId

    override fun onAdded() {
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
        return if (throwable is SocketTimeoutException) {
            RetryConstraint.createExponentialBackoff(runCount, AppConfig.INITIAL_BACK_OFF_IN_MS)
        } else {
            RetryConstraint.CANCEL
        }
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
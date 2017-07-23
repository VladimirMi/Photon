package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.CancelReason
import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.AppConfig
import io.github.vladimirmi.photon.utils.ErrorObserver
import io.reactivex.schedulers.Schedulers
import java.net.SocketTimeoutException
import java.util.*

/**
 * Created by Vladimir Mikhalev 18.07.2017.
 */


class DeleteAlbumJob(private val albumId: String,
                     private val skipNetworkPart: Boolean)
    : Job(Params(JobPriority.MID)
        .setSingleId(albumId)
        .requireNetwork()
        .persist()) {


    override fun onAdded() {
        val dataManager = DaggerService.appComponent.dataManager()
        val cache = DaggerService.appComponent.cache()

        cache.removeAlbum(albumId)
        dataManager.removeFromDb(Album::class.java, albumId)
    }

    override fun onRun() {
        if (skipNetworkPart) return
        var error: Throwable? = null
        val dataManager = DaggerService.appComponent.dataManager()

        dataManager.deleteAlbum(albumId)
                .blockingSubscribe({}, { error = it })

        error?.let { throw it }
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        logCancel(cancelReason, throwable)
        if (cancelReason == CancelReason.CANCELLED_VIA_SHOULD_RE_RUN) {
            updateAlbum()
        }
    }

    private fun updateAlbum() {
        val dataManager = DaggerService.appComponent.dataManager()

        dataManager.getAlbumFromNet(albumId, Date(0).toString())
                .doOnNext { dataManager.saveToDB(it) }
                .subscribeOn(Schedulers.io())
                .subscribeWith(ErrorObserver())
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int): RetryConstraint {
        return if (throwable is SocketTimeoutException) {
            RetryConstraint.createExponentialBackoff(runCount, AppConfig.INITIAL_BACK_OFF_IN_MS)
        } else {
            RetryConstraint.CANCEL
        }
    }
}

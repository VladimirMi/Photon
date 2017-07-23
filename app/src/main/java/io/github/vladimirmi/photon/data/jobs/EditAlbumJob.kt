package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.CancelReason
import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import io.github.vladimirmi.photon.data.models.EditAlbumReq
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.AppConfig
import io.github.vladimirmi.photon.utils.ErrorObserver
import io.reactivex.schedulers.Schedulers
import java.net.SocketTimeoutException
import java.util.*

/**
 * Created by Vladimir Mikhalev 20.07.2017.
 */

class EditAlbumJob(private val albumReq: EditAlbumReq)
    : Job(Params(JobPriority.HIGH)
        .addTags(TAG + albumReq.id)
        .requireNetwork()
        .persist()) {

    companion object {
        const val TAG = "EditAlbumJobTag"
    }

    val tag = EditAlbumJob.TAG + albumReq.id

    override fun onAdded() {
        val dataManager = DaggerService.appComponent.dataManager()
        val album = dataManager.getDetachedObjFromDb(Album::class.java, albumReq.id)!!.apply {
            title = albumReq.title
            description = albumReq.description
        }
        dataManager.saveToDB(album)
    }

    override fun onRun() {
        val dataManager = DaggerService.appComponent.dataManager()

        var error: Throwable? = null
        dataManager.editAlbum(albumReq)
                .doOnNext { dataManager.saveToDB(it) }
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

        dataManager.getAlbumFromNet(albumReq.id, Date(0).toString())
                .subscribeOn(Schedulers.io())
                .subscribeWith(object : ErrorObserver<Album>() {
                    override fun onNext(it: Album) {
                        dataManager.saveToDB(it)
                    }
                })
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int): RetryConstraint {
        return if (throwable is SocketTimeoutException) {
            RetryConstraint.createExponentialBackoff(runCount, AppConfig.INITIAL_BACK_OFF_IN_MS)
        } else {
            RetryConstraint.CANCEL
        }
    }
}
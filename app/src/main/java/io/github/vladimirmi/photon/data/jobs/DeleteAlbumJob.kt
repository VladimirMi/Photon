package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.CancelReason
import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.ErrorObserver
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * Created by Vladimir Mikhalev 18.07.2017.
 */


class DeleteAlbumJob(private val albumId: String)
    : Job(Params(JobPriority.MID)
        .setSingleId(albumId)
        .requireNetwork()
        .persist()) {

    private val photosId = ArrayList<String>()

    override fun onAdded() {
        val dataManager = DaggerService.appComponent.dataManager()
        val cache = DaggerService.appComponent.cache()

        cache.removeAlbum(albumId)
        dataManager.removeFromDb(Album::class.java, albumId)
    }

    override fun onRun() {
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
                .subscribeOn(Schedulers.io())
                .subscribeWith(object : ErrorObserver<Album>() {
                    override fun onNext(it: Album) {
                        dataManager.saveToDB(it)
                    }
                })
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int): RetryConstraint {
        return RetryConstraint.CANCEL
    }
}

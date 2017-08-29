package io.github.vladimirmi.photon.data.jobs.album

import com.birbit.android.jobqueue.CancelReason
import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.req.AlbumEditReq
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.*
import io.reactivex.schedulers.Schedulers

/**
 * Created by Vladimir Mikhalev 20.07.2017.
 */

class AlbumEditJob(private val albumId: String)
    : Job(Params(JobPriority.LOW)
        .setGroupId(JobGroup.ALBUM)
        .addTags(TAG)
        .requireNetwork()
        .persist()) {

    companion object {
        const val TAG = "AlbumEditJob"
    }

    override fun onAdded() {}

    override fun onRun() {
        val dataManager = DaggerService.appComponent.dataManager()
        var error: Throwable? = null
        val album = dataManager.getDetachedObjFromDb(Album::class.java, albumId)!!
        val request = AlbumEditReq.fromAlbum(album)

        dataManager.editAlbum(request)
                .blockingSubscribe({}, { error = it })

        error?.let { throw it }
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        logCancel(cancelReason, throwable)
        if (cancelReason == CancelReason.CANCELLED_VIA_SHOULD_RE_RUN) {
            updateAlbum()
        }
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int) =
            cancelOrWaitConnection(throwable, runCount)

    private fun updateAlbum() {
        val dataManager = DaggerService.appComponent.dataManager()

        dataManager.getAlbumFromNet(id, "0")
                .doOnNext { dataManager.saveFromNet(it) }
                .subscribeOn(Schedulers.io())
                .subscribeWith(ErrorObserver())
    }
}
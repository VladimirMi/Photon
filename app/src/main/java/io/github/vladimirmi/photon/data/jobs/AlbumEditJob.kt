package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.CancelReason
import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.req.EditAlbumReq
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.*
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * Created by Vladimir Mikhalev 20.07.2017.
 */

class AlbumEditJob(val request: EditAlbumReq,
                   val skipNetworkPart: Boolean = false)
    : Job(Params(JobPriority.LOW)
        .setDelayMs(1000)
        .setGroupId(JobGroup.ALBUM)
        .addTags(TAG + request.id, JobGroup.ALBUM + request.id)
        .requireNetwork()
        .persist()) {

    companion object {
        const val TAG = "AlbumEditJob"
    }

    override fun onAdded() {
        val dataManager = DaggerService.appComponent.dataManager()
        val album = dataManager.getDetachedObjFromDb(Album::class.java, request.id)!!.apply {
            title = request.title
            description = request.description
        }
        dataManager.saveToDB(album)
    }

    override fun onRun() {
        if (skipNetworkPart) return
        val dataManager = DaggerService.appComponent.dataManager()

        var error: Throwable? = null
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

    private fun updateAlbum() {
        val dataManager = DaggerService.appComponent.dataManager()

        dataManager.getAlbumFromNet(request.id, Date(0).toString())
                .subscribeOn(Schedulers.io())
                .subscribeWith(object : ErrorObserver<Album>() {
                    override fun onNext(it: Album) {
                        dataManager.saveToDB(it)
                    }
                })
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int): RetryConstraint {
        return cancelOrWait(throwable, runCount)
    }
}
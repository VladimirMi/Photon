package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.CancelReason
import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import io.github.vladimirmi.photon.data.jobs.queue.JobTask
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.req.AlbumEditReq
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.*
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * Created by Vladimir Mikhalev 20.07.2017.
 */

class AlbumEditJob(override val request: AlbumEditReq)
    : Job(Params(JobPriority.LOW)
        .setGroupId(JobGroup.ALBUM)
        .requireNetwork()
        .persist()), JobTask {

    companion object {
        const val TAG = "AlbumEditJob"
    }

    override var entityId = request.id
    override var parentEntityId
        get() = entityId
        set(value) {
            entityId = value
        }
    override val tag = TAG
    override val type = JobTask.Type.UNIQUE

    override fun onQueued() {
        val dataManager = DaggerService.appComponent.dataManager()
        val album = dataManager.getDetachedObjFromDb(Album::class.java, request.id)!!.apply {
            title = request.title
            description = request.description
        }
        dataManager.saveToDB(album)
    }

    override fun onAdded() {}

    override fun onRun() {
        val dataManager = DaggerService.appComponent.dataManager()
        request.id = entityId

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

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int) =
            cancelOrWait(throwable, runCount)

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
}
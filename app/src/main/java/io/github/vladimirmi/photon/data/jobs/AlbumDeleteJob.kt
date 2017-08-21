package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.CancelReason
import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import io.github.vladimirmi.photon.data.jobs.queue.JobTask
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.*
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * Created by Vladimir Mikhalev 18.07.2017.
 */


class AlbumDeleteJob(albumId: String)
    : Job(Params(JobPriority.HIGH)
        .setGroupId(JobGroup.ALBUM)
        .setSingleId(albumId)
        .requireNetwork()
        .persist()), JobTask {

    companion object {
        const val TAG = "AlbumDeleteJob"
    }

    override var entityId = albumId
    override var parentEntityId
        get() = entityId
        set(value) {
            entityId = value
        }
    override val tag = TAG
    override val type = JobTask.Type.DELETE

    override fun onQueued() {
        DaggerService.appComponent.dataManager().removeFromDb(Album::class.java, entityId)
        DaggerService.appComponent.cache().removeAlbum(entityId)
    }

    override fun onAdded() {}

    override fun onRun() {
        var error: Throwable? = null
        val dataManager = DaggerService.appComponent.dataManager()

        dataManager.deleteAlbum(entityId)
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

        dataManager.getAlbumFromNet(parentEntityId, Date(0).toString())
                .doOnNext { dataManager.saveToDB(it) }
                .subscribeOn(Schedulers.io())
                .subscribeWith(ErrorObserver())
    }
}

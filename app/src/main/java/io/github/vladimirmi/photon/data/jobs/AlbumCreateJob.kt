package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import io.github.vladimirmi.photon.data.jobs.queue.JobTask
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.data.models.req.NewAlbumReq
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.JobGroup
import io.github.vladimirmi.photon.utils.JobPriority
import io.github.vladimirmi.photon.utils.cancelOrWait
import io.github.vladimirmi.photon.utils.logCancel

/**
 * Created by Vladimir Mikhalev 17.07.2017.
 */

class AlbumCreateJob(override val request: NewAlbumReq)
    : Job(Params(JobPriority.HIGH)
        .setGroupId(JobGroup.ALBUM)
        .requireNetwork()
        .persist()), JobTask {

    companion object {
        const val TAG = "AlbumCreateJob"
    }

    override var entityId = request.id
    override var parentEntityId = "ROOT"
    override val tag = TAG
    override val type = JobTask.Type.CREATE

    override fun onQueued() {
        val dataManager = DaggerService.appComponent.dataManager()

        val profile = dataManager.getDetachedObjFromDb(User::class.java, dataManager.getProfileId())!!
        val album = Album(id = request.id, owner = request.owner,
                title = request.title, description = request.description)
        profile.albums.add(album)
        dataManager.saveToDB(profile)
    }

    override fun onAdded() {}

    override fun onRun() {
        val dataManager = DaggerService.appComponent.dataManager()
        var error: Throwable? = null

        dataManager.createAlbum(request)
                .doOnNext {
                    entityId = it.id
                    val profile = dataManager.getDetachedObjFromDb(User::class.java, dataManager.getProfileId())!!
                    profile.albums.add(it)
                    dataManager.saveToDB(profile)
                    deleteLocalAlbum()
                }
                .blockingSubscribe({}, { error = it })

        error?.let { throw it }
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        logCancel(cancelReason, throwable)
        if (throwable != null) {
            deleteLocalAlbum()
        }
    }


    private fun deleteLocalAlbum() {
        DaggerService.appComponent.dataManager().removeFromDb(Album::class.java, entityId)
        DaggerService.appComponent.cache().removeAlbum(entityId)
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int) =
            cancelOrWait(throwable, runCount)
}
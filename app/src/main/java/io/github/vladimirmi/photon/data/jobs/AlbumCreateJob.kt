package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.data.models.req.NewAlbumReq
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.*

/**
 * Created by Vladimir Mikhalev 17.07.2017.
 */

class AlbumCreateJob(val request: NewAlbumReq)
    : Job(Params(JobPriority.HIGH)
        .setGroupId(JobGroup.ALBUM)
        .addTags(TAG + request.id, JobGroup.ALBUM + request.id)
        .requireNetwork()
        .persist()), WithPayload {

    companion object {
        const val TAG = "AlbumCreateJob"
    }

    override val payload = Payload<String>()

    override fun onAdded() {
        val dataManager = DaggerService.appComponent.dataManager()
        val profile = dataManager.getDetachedObjFromDb(User::class.java, dataManager.getProfileId())!!
        val album = Album(id = request.id, owner = request.owner,
                title = request.title, description = request.description)
        profile.albums.add(album)
        dataManager.saveToDB(profile)
    }

    override fun onRun() {
        val dataManager = DaggerService.appComponent.dataManager()
        var error: Throwable? = null

        dataManager.createAlbum(request)
                .doOnNext {
                    payload.value = it.id
                    val profile = dataManager.getDetachedObjFromDb(User::class.java, dataManager.getProfileId())!!
                    profile.albums.add(it)
                    dataManager.saveToDB(profile)
                }
                .blockingSubscribe({}, { error = it })

        error?.let { throw it }
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        logCancel(cancelReason, throwable)
        if (throwable != null) {
            removeTempAlbum()
        }
    }

    fun removeTempAlbum() {
        DaggerService.appComponent.dataManager().removeFromDb(Album::class.java, request.id)
        DaggerService.appComponent.cache().removeAlbum(request.id)
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int): RetryConstraint {
        return RetryConstraint.createExponentialBackoff(runCount, AppConfig.INITIAL_BACK_OFF_IN_MS)
    }

}
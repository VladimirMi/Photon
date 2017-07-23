package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import io.github.vladimirmi.photon.data.models.NewAlbumReq
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.AppConfig
import java.net.SocketTimeoutException

/**
 * Created by Vladimir Mikhalev 17.07.2017.
 */

class CreateAlbumJob(private val request: NewAlbumReq)
    : Job(Params(JobPriority.HIGH)
        .setGroupId(TAG)
        .addTags(TAG + request.id)
        .requireNetwork()
        .persist()) {

    companion object {
        const val TAG = "CreateAlbumJobTag"
    }

    val tag = TAG + request.id

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
        val cache = DaggerService.appComponent.cache()
        var error: Throwable? = null

        dataManager.createAlbum(request)
                .doOnNext {
                    dataManager.removeFromDb(Album::class.java, request.id)
                    cache.removeAlbum(request.id)

                    val profile = dataManager.getDetachedObjFromDb(User::class.java, dataManager.getProfileId())!!
                    profile.albums.add(it)
                    dataManager.saveToDB(profile)
                }
                .blockingSubscribe({}, { error = it })

        error?.let { throw it }
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        logCancel(cancelReason, throwable)
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int): RetryConstraint {
        return if (throwable is SocketTimeoutException) {
            RetryConstraint.createExponentialBackoff(runCount, AppConfig.INITIAL_BACK_OFF_IN_MS)
        } else {
            RetryConstraint.CANCEL
        }
    }

}
package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import io.github.vladimirmi.photon.data.models.NewAlbumReq
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.Constants

/**
 * Created by Vladimir Mikhalev 17.07.2017.
 */

class CreateAlbumJob(private val request: NewAlbumReq)
    : Job(Params(JobPriority.MID)
        .setGroupId("CreateAlbum")
        .addTags(Constants.CREATE_ALBUM_JOB_TAG + request.id)
        .requireNetwork()
        .persist()) {
    override fun onAdded() {}

    override fun onRun() {
        val dataManager = DaggerService.appComponent.dataManager()
        val cache = DaggerService.appComponent.cache()
        var error: Throwable? = null

        dataManager.createAlbum(request)
                .doOnNext {
                    //todo save and remove to DB through cache abstraction
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
        return RetryConstraint.CANCEL
    }

}
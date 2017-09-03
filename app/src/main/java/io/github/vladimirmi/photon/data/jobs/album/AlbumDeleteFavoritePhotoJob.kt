package io.github.vladimirmi.photon.data.jobs.album

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import io.github.vladimirmi.photon.data.managers.extensions.JobPriority
import io.github.vladimirmi.photon.data.managers.extensions.cancelOrWaitConnection
import io.github.vladimirmi.photon.data.managers.extensions.getAlbum
import io.github.vladimirmi.photon.data.managers.extensions.logCancel
import io.github.vladimirmi.photon.data.models.realm.extensions.addFavorite
import io.github.vladimirmi.photon.di.DaggerService

/**
 * Created by Vladimir Mikhalev 21.07.2017.
 */

class AlbumDeleteFavoritePhotoJob(private val photocardId: String)
    : Job(Params(JobPriority.HIGH)
        .addTags(TAG + photocardId)
        .requireNetwork()) {

    companion object {
        const val TAG = "AlbumDeleteFavoritePhotoJob"
    }

    private val dataManager = DaggerService.appComponent.dataManager()

    override fun onAdded() {}

    override fun onRun() {
        dataManager.removeFromFavorite(photocardId).blockingGet()
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int) =
            cancelOrWaitConnection(throwable, runCount)

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        logCancel(cancelReason, throwable)
        rollback()
    }

    private fun rollback() {
        dataManager.getAlbum(dataManager.getUserFavAlbumId()).addFavorite(photocardId)
    }
}
package io.github.vladimirmi.photon.data.jobs.album

import io.github.vladimirmi.photon.data.jobs.ChainJob
import io.github.vladimirmi.photon.data.jobs.photocard.PhotocardCreateJob
import io.github.vladimirmi.photon.data.managers.extensions.JobGroup
import io.github.vladimirmi.photon.di.DaggerService

/**
 * Created by Vladimir Mikhalev 21.07.2017.
 */

class AlbumAddFavoritePhotoJob(private val photocardId: String)
    : ChainJob(TAG, JobGroup.ALBUM, photocardId) {

    companion object {
        const val TAG = "AlbumAddFavoritePhotoJob"
    }

    override val needCreate = listOf(PhotocardCreateJob.TAG + photocardId)
    override val needCancel = AlbumDeleteFavoritePhotoJob.TAG + photocardId

    override fun execute() {
        val repository = DaggerService.appComponent.albumJobRepository()
        repository.addToFavorite(result ?: photocardId).blockingGet()
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        super.onCancel(cancelReason, throwable)

        val repository = DaggerService.appComponent.albumJobRepository()
        repository.rollbackAddFavorite(photocardId)
    }

    override fun copy() = throw UnsupportedOperationException()
}
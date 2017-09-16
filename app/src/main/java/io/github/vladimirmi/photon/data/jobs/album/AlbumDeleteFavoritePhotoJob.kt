package io.github.vladimirmi.photon.data.jobs.album

import io.github.vladimirmi.photon.data.jobs.ChainJob
import io.github.vladimirmi.photon.data.jobs.photocard.PhotocardCreateJob
import io.github.vladimirmi.photon.data.managers.extensions.JobGroup
import io.github.vladimirmi.photon.di.DaggerService

/**
 * Created by Vladimir Mikhalev 21.07.2017.
 */

class AlbumDeleteFavoritePhotoJob(private val photocardId: String)
    : ChainJob(TAG, JobGroup.ALBUM, photocardId) {

    companion object {
        const val TAG = "AlbumDeleteFavoritePhotoJob"
    }

    override val needCreate = listOf(PhotocardCreateJob.TAG + photocardId)
    override val needCancel = AlbumAddFavoritePhotoJob.TAG + photocardId

    override fun onStart() {
        val repository = DaggerService.appComponent.albumJobRepository()
        repository.removeFromFavorite(result ?: photocardId).blockingGet()
    }

    override fun onError(throwable: Throwable) {
        val repository = DaggerService.appComponent.albumJobRepository()
        repository.rollbackRemoveFavorite(photocardId)
    }

    override fun copy() = throw UnsupportedOperationException()
}
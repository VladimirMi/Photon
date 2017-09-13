package io.github.vladimirmi.photon.data.jobs.album

import io.github.vladimirmi.photon.data.jobs.ChainJob
import io.github.vladimirmi.photon.data.repository.album.AlbumJobRepository

/**
 * Created by Vladimir Mikhalev 21.07.2017.
 */

class AlbumAddFavoritePhotoJob(private val photocardId: String,
                               private val repository: AlbumJobRepository)
    : ChainJob(TAG, photocardId) {

    companion object {
        const val TAG = "AlbumAddFavoritePhotoJob"
    }

    override fun onRun() {
        repository.addToFavorite(photocardId).blockingGet()
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        super.onCancel(cancelReason, throwable)
        repository.rollbackAddFavorite(photocardId)
    }
}
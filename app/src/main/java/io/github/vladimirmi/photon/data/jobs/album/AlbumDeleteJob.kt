package io.github.vladimirmi.photon.data.jobs.album

import io.github.vladimirmi.photon.data.jobs.ChainJob
import io.github.vladimirmi.photon.data.repository.album.AlbumJobRepository

/**
 * Created by Vladimir Mikhalev 18.07.2017.
 */


class AlbumDeleteJob(private val albumId: String,
                     private val repository: AlbumJobRepository)
    : ChainJob(TAG, albumId) {

    companion object {
        const val TAG = "AlbumDeleteJob"
    }

    override fun onRun() {
        repository.delete(albumId).blockingGet()
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        super.onCancel(cancelReason, throwable)
        repository.rollbackDelete(albumId)
    }
}

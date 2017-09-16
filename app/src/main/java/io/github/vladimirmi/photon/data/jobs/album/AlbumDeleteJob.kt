package io.github.vladimirmi.photon.data.jobs.album

import io.github.vladimirmi.photon.data.jobs.ChainJob
import io.github.vladimirmi.photon.data.jobs.QueueJobHolder
import io.github.vladimirmi.photon.data.jobs.photocard.PhotocardDeleteJob
import io.github.vladimirmi.photon.data.managers.extensions.JobGroup
import io.github.vladimirmi.photon.di.DaggerService

/**
 * Created by Vladimir Mikhalev 18.07.2017.
 */

class AlbumDeleteJob(private val albumId: String)
    : ChainJob(TAG, JobGroup.ALBUM, albumId) {

    companion object {
        const val TAG = "AlbumDeleteJob"
    }

    override val needCancel = groupTag

    override fun onStart() {
        val repository = DaggerService.appComponent.albumJobRepository()
        val album = repository.getAlbum(albumId)
        queue.addAll(album.photocards.map {
            QueueJobHolder(PhotocardDeleteJob.TAG, JobGroup.PHOTOCARD, it.id)
        })
        repository.delete(albumId).blockingGet()
    }

    override fun onError(throwable: Throwable) {
        val repository = DaggerService.appComponent.albumJobRepository()
        repository.rollbackDelete(albumId)
    }

    override fun copy() = throw UnsupportedOperationException()
}

package io.github.vladimirmi.photon.data.jobs.album

import io.github.vladimirmi.photon.data.jobs.ChainJob
import io.github.vladimirmi.photon.data.managers.extensions.JobGroup
import io.github.vladimirmi.photon.data.models.req.AlbumNewReq
import io.github.vladimirmi.photon.di.DaggerService

/**
 * Created by Vladimir Mikhalev 17.07.2017.
 */

class AlbumCreateJob(private val albumId: String)
    : ChainJob(TAG, JobGroup.ALBUM, albumId) {

    companion object {
        const val TAG = "AlbumCreateJob"
    }

    override fun execute() {
        val repository = DaggerService.appComponent.albumJobRepository()
        val album = repository.getAlbum(albumId)
        val request = AlbumNewReq.fromAlbum(album)

        val albumRes = repository.create(request).blockingGet()

        album.id = albumRes.id
        result = album.id
        repository.save(album)
    }

    override fun copy() = AlbumCreateJob(albumId).apply {
        queue.addAll(this@AlbumCreateJob.queue)
    }
}
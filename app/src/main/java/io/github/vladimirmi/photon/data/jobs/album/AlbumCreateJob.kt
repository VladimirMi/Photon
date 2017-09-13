package io.github.vladimirmi.photon.data.jobs.album

import io.github.vladimirmi.photon.data.jobs.ChainJob
import io.github.vladimirmi.photon.data.models.req.AlbumNewReq
import io.github.vladimirmi.photon.data.repository.album.AlbumJobRepository

/**
 * Created by Vladimir Mikhalev 17.07.2017.
 */

class AlbumCreateJob(private val albumId: String,
                     private val repository: AlbumJobRepository)
    : ChainJob(TAG, albumId) {

    companion object {
        const val TAG = "AlbumCreateJob"
    }

    override fun onRun() {
        val album = repository.getAlbum(albumId)
        val request = AlbumNewReq.fromAlbum(album)

        val albumRes = repository.create(request).blockingGet()

        album.id = albumRes.id
        repository.save(album)
    }
}
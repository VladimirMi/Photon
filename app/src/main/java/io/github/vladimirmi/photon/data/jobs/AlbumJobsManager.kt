package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.Job
import io.github.vladimirmi.photon.data.jobs.album.*
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.req.AlbumEditReq
import io.github.vladimirmi.photon.data.repository.album.AlbumJobRepository

/**
 * Created by Vladimir Mikhalev 31.08.2017.
 */

class AlbumJobsManager(repository: AlbumJobRepository)
    : BaseJobsManager<Album, AlbumJobRepository>(repository) {

    override fun getFromServer(id: String): Album =
            repository.getAlbumFromNet(id).blockingGet()

    override fun getJob(client: Album, server: Album): Job? =
            getEditJob(client, server) ?:
                    getAddOrDeleteFavoriteJob(client, server)


    override fun getCreateJob(client: Album): Job? = AlbumCreateJob(client.id, repository)

    override fun getDeleteJob(client: Album): Job? = AlbumDeleteJob(client.id, repository)

    private fun getEditJob(client: Album, server: Album): Job? =
            if (isEdited(server, client)) {
                AlbumEditJob(client.id, repository)
            } else null

    private fun getAddOrDeleteFavoriteJob(client: Album, server: Album): Job? {
        if (!client.isFavorite || server.photocards.size == client.photocards.size) return null

        val serverPhotocards = server.photocards.map { it.id }.toMutableList()
        val clientPhotocards = client.photocards.map { it.id }.toMutableList()

        return if (server.photocards.size > client.photocards.size) {
            serverPhotocards.removeAll(clientPhotocards)
            val deleteId = serverPhotocards.first()
            AlbumDeleteFavoritePhotoJob(deleteId, repository)
        } else {
            clientPhotocards.removeAll(serverPhotocards)
            val addId = clientPhotocards.first()
            AlbumAddFavoritePhotoJob(addId, repository)
        }
    }

    private fun isEdited(client: Album, server: Album): Boolean =
            AlbumEditReq.from(client) != AlbumEditReq.from(server)
}

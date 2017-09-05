package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.Job
import io.github.vladimirmi.photon.data.jobs.album.*
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.req.AlbumEditReq
import io.github.vladimirmi.photon.data.repository.album.AlbumJobRepository

/**
 * Created by Vladimir Mikhalev 31.08.2017.
 */

class AlbumJobsManager(private val repository: AlbumJobRepository) : BaseJobsManager<Album>() {

    override fun getFromServer(id: String): Album =
            repository.getAlbumFromNet(id).blockingGet()

    override fun getJob(client: Album, server: Album): Job? =
            getEditJob(client, server) ?:
                    getAddOrDeleteFavoriteJob(client, server)


    override fun getCreateJob(id: String): Job? =
            if (canRun(AlbumCreateJob.TAG + id)) AlbumCreateJob(id, repository) else null

    override fun getDeleteJob(id: String): Job? =
            if (canRun(AlbumDeleteJob.TAG + id)) AlbumDeleteJob(id, repository) else null

    private fun getEditJob(client: Album, server: Album): Job? =
            if (isEdited(server, client) && canRun(AlbumEditJob.TAG + client.id)) {
                AlbumEditJob(client.id, repository)
            } else null

    private fun getAddOrDeleteFavoriteJob(client: Album, server: Album): Job? {
        if (!client.isFavorite || server.photocards.size == client.photocards.size) return null

        val serverPhotocards = server.photocards.map { it.id }.toMutableList()
        val clientPhotocards = client.photocards.map { it.id }.toMutableList()

        if (server.photocards.size > client.photocards.size) {
            serverPhotocards.removeAll(clientPhotocards)
            val deleteId = serverPhotocards.first()
            if (canRun(AlbumDeleteFavoritePhotoJob.TAG + deleteId))
                return AlbumDeleteFavoritePhotoJob(deleteId, repository)
        } else {
            clientPhotocards.removeAll(serverPhotocards)
            val addId = clientPhotocards.first()
            if (canRun(AlbumAddFavoritePhotoJob.TAG + addId))
                return AlbumAddFavoritePhotoJob(addId, repository)
        }
        return null
    }

    private fun isEdited(client: Album, server: Album): Boolean =
            AlbumEditReq.from(client) != AlbumEditReq.from(server)

    override fun saveSync(obj: Album) {
        obj.sync = true
        repository.save(obj)
    }
}

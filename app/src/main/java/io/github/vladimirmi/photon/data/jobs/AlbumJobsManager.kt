package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.Job
import io.github.vladimirmi.photon.data.jobs.album.*
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.req.AlbumEditReq

/**
 * Created by Vladimir Mikhalev 31.08.2017.
 */

class AlbumJobsManager(dataManager: DataManager) : BaseJobsManager<Album>(dataManager) {

    override fun getFromServer(id: String): Album =
            dataManager.getAlbumFromNet(id, force = true).blockingFirst()

    override fun getJob(client: Album, server: Album): Job? =
            getEditJob(client, server) ?:
                    getAddOrDeleteFavoriteJob(client, server)


    override fun getCreateJob(id: String): Job? =
            if (canRun(AlbumCreateJob.TAG + id)) AlbumCreateJob(id) else null

    override fun getDeleteJob(id: String): Job? =
            if (canRun(AlbumDeleteJob.TAG + id)) AlbumDeleteJob(id) else null

    private fun getEditJob(client: Album, server: Album): Job? =
            if (isEdited(server, client) && canRun(AlbumEditJob.TAG + client.id)) {
                AlbumEditJob(client.id)
            } else null

    private fun getAddOrDeleteFavoriteJob(client: Album, server: Album): Job? {
        if (!client.isFavorite || server.photocards.size == client.photocards.size) return null

        val serverPhotocards = server.photocards.map { it.id }.toMutableList()
        val clientPhotocards = client.photocards.map { it.id }.toMutableList()

        if (server.photocards.size > client.photocards.size) {
            serverPhotocards.removeAll(clientPhotocards)
            val deleteId = serverPhotocards.first()
            if (canRun(AlbumDeleteFavoritePhotoJob.TAG + deleteId)) return AlbumDeleteFavoritePhotoJob(deleteId)
        } else {
            clientPhotocards.removeAll(serverPhotocards)
            val addId = clientPhotocards.first()
            if (canRun(AlbumAddFavoritePhotoJob.TAG + addId)) return AlbumAddFavoritePhotoJob(addId)
        }
        return null
    }

    private fun isEdited(client: Album, server: Album): Boolean =
            AlbumEditReq.from(client) != AlbumEditReq.from(server)
}

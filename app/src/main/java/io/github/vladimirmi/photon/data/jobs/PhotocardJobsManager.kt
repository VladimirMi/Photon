package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.Job
import io.github.vladimirmi.photon.data.jobs.album.AlbumCreateJob
import io.github.vladimirmi.photon.data.jobs.album.AlbumDeleteJob
import io.github.vladimirmi.photon.data.jobs.photocard.PhotocardAddViewJob
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.realm.Photocard

/**
 * Created by Vladimir Mikhalev 31.08.2017.
 */

class PhotocardJobsManager(dataManager: DataManager) : BaseJobsManager<Photocard>(dataManager) {

    override fun getFromServer(id: String): Photocard =
            dataManager.getPhotocardFromNet(id, force = true).blockingFirst()

    override fun getJob(client: Photocard, server: Photocard): Job? =
            getEditJob(client, server) ?: getAddViewJob(client, server)

    override fun getCreateJob(id: String): Job? =
            if (canRun(AlbumCreateJob.TAG + id)) AlbumCreateJob(id) else null

    override fun getDeleteJob(id: String): Job? =
            if (canRun(AlbumDeleteJob.TAG + id)) AlbumDeleteJob(id) else null

    private fun getEditJob(client: Photocard, server: Photocard): Job? = null
//            if (client != server && canRun(AlbumEditJob.TAG + client.id)) {
//                AlbumEditJob(client.id)
//            } else null

    private fun getAddViewJob(client: Photocard, server: Photocard): Job? =
            if (client.views > server.views && canRun(PhotocardAddViewJob.TAG + client.id)) {
                PhotocardAddViewJob(client.id)
            } else null
}
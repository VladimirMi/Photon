package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.Job
import io.github.vladimirmi.photon.data.jobs.photocard.PhotocardAddViewJob
import io.github.vladimirmi.photon.data.jobs.photocard.PhotocardCreateJob
import io.github.vladimirmi.photon.data.jobs.photocard.PhotocardDeleteJob
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.models.realm.isTemp
import io.github.vladimirmi.photon.data.repository.photocard.PhotocardJobRepository

/**
 * Created by Vladimir Mikhalev 31.08.2017.
 */

class PhotocardJobsManager(repository: PhotocardJobRepository)
    : BaseJobsManager<Photocard, PhotocardJobRepository>(repository) {

    override fun getFromServer(id: String): Photocard =
            repository.getPhotocardFromNet(id).blockingGet()

    override fun getJob(client: Photocard, server: Photocard): Job? =
            getEditJob(client, server) ?: getAddViewJob(client, server)

    override fun getCreateJob(client: Photocard): Job? =
            if (!client.album.isTemp()) PhotocardCreateJob(client.id, repository) else null

    override fun getDeleteJob(client: Photocard): Job? = PhotocardDeleteJob(client.id, repository)

    private fun getEditJob(client: Photocard, server: Photocard): Job? = null
//            if (client != server && canRun(AlbumEditJob.TAG + client.id)) {
//                AlbumEditJob(client.id)
//            } else null

    private fun getAddViewJob(client: Photocard, server: Photocard): Job? =
            if (client.views > server.views) {
                PhotocardAddViewJob(client.id, repository)
            } else null
}

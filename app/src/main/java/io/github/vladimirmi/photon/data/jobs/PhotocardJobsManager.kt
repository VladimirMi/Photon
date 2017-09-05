package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.Job
import io.github.vladimirmi.photon.data.jobs.photocard.PhotocardAddViewJob
import io.github.vladimirmi.photon.data.jobs.photocard.PhotocardCreateJob
import io.github.vladimirmi.photon.data.jobs.photocard.PhotocardDeleteJob
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.repository.photocard.PhotocardJobRepository

/**
 * Created by Vladimir Mikhalev 31.08.2017.
 */

class PhotocardJobsManager(private val repository: PhotocardJobRepository) : BaseJobsManager<Photocard>() {

    override fun getFromServer(id: String): Photocard =
            repository.getPhotocardFromNet(id).blockingGet()

    override fun getJob(client: Photocard, server: Photocard): Job? =
            getEditJob(client, server) ?: getAddViewJob(client, server)

    override fun getCreateJob(id: String): Job? =
            if (canRun(PhotocardCreateJob.TAG + id)) PhotocardCreateJob(id, repository) else null

    override fun getDeleteJob(id: String): Job? =
            if (canRun(PhotocardDeleteJob.TAG + id)) PhotocardDeleteJob(id, repository) else null

    private fun getEditJob(client: Photocard, server: Photocard): Job? = null
//            if (client != server && canRun(AlbumEditJob.TAG + client.id)) {
//                AlbumEditJob(client.id)
//            } else null

    private fun getAddViewJob(client: Photocard, server: Photocard): Job? =
            if (client.views > server.views && canRun(PhotocardAddViewJob.TAG + client.id)) {
                PhotocardAddViewJob(client.id, repository)
            } else null

    override fun saveSync(obj: Photocard) {
        obj.sync = true
        repository.save(obj)
    }
}
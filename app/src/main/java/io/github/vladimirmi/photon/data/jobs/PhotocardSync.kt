package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.JobManager
import io.github.vladimirmi.photon.data.jobs.photocard.PhotocardAddViewJob
import io.github.vladimirmi.photon.data.jobs.photocard.PhotocardCreateJob
import io.github.vladimirmi.photon.data.jobs.photocard.PhotocardDeleteJob
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.realm.Photocard
import timber.log.Timber

/**
 * Created by Vladimir Mikhalev 29.08.2017.
 */

class PhotocardSync(private val jobManager: JobManager,
                    private val dataManager: DataManager) {

    fun create(localPhotocard: Photocard) {
        Timber.e("create: ")
        if (!localPhotocard.canCreate()) return
        saveSync(localPhotocard)
        jobManager.addJobInBackground(PhotocardCreateJob(localPhotocard.id))
    }

    fun delete(localPhotocard: Photocard) {
        Timber.e("delete: ")
        saveSync(localPhotocard)
        jobManager.addJobInBackground(PhotocardDeleteJob(localPhotocard.id))
    }

    fun edit(localPhotocard: Photocard) {
        Timber.e("edit: ")
        saveSync(localPhotocard)

        val net = dataManager.getPhotocardFromNet(localPhotocard.id, "0", "0")
                .blockingFirst()

        getEditJobs(localPhotocard, net).forEach { jobManager.addJobInBackground(it) }
    }

    private fun getEditJobs(local: Photocard, net: Photocard): List<Job> {
        val jobs = ArrayList<Job>()

        (1..local.views - net.views).forEach { jobs.add(PhotocardAddViewJob(local.id)) }
        return jobs
    }


    private fun saveSync(localPhotocard: Photocard) {
        localPhotocard.sync = true
        dataManager.save(localPhotocard)
    }
}
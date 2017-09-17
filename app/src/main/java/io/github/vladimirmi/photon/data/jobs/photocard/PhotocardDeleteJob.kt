package io.github.vladimirmi.photon.data.jobs.photocard

import io.github.vladimirmi.photon.data.jobs.ChainJob
import io.github.vladimirmi.photon.data.managers.utils.JobGroup
import io.github.vladimirmi.photon.di.DaggerService

/**
 * Created by Vladimir Mikhalev 18.07.2017.
 */

class PhotocardDeleteJob(private val photocardId: String)
    : ChainJob(TAG, JobGroup.PHOTOCARD, photocardId) {

    companion object {
        const val TAG = "PhotocardDeleteJob"
    }

    override val needCancel = groupTag

    override fun onStart() {
        val repository = DaggerService.appComponent.photocardJobRepository()
        repository.delete(photocardId).blockingGet()
    }

    override fun onError(throwable: Throwable) {
        val repository = DaggerService.appComponent.photocardJobRepository()
        repository.rollbackDelete(photocardId)
    }

    override fun copy() = throw UnsupportedOperationException()
}
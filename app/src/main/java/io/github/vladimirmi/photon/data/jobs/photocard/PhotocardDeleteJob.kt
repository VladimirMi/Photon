package io.github.vladimirmi.photon.data.jobs.photocard

import io.github.vladimirmi.photon.data.jobs.ChainJob
import io.github.vladimirmi.photon.data.managers.extensions.JobGroup
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

    override fun execute() {
        val repository = DaggerService.appComponent.photocardJobRepository()
        repository.delete(photocardId).blockingGet()
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        super.onCancel(cancelReason, throwable)
        val repository = DaggerService.appComponent.photocardJobRepository()
        repository.rollbackDelete(photocardId)
    }

    override fun copy() = throw UnsupportedOperationException()
}
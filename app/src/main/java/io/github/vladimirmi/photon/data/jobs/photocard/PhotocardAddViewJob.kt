package io.github.vladimirmi.photon.data.jobs.photocard

import io.github.vladimirmi.photon.data.jobs.ChainJob
import io.github.vladimirmi.photon.data.repository.photocard.PhotocardJobRepository

/**
 * Created by Vladimir Mikhalev 21.07.2017.
 */

class PhotocardAddViewJob(private val photocardId: String,
                          private val repository: PhotocardJobRepository)
    : ChainJob(TAG, photocardId) {

    companion object {
        const val TAG = "PhotocardAddViewJob"
    }

    override fun onRun() {
        repository.addView(photocardId).blockingGet()
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        super.onCancel(cancelReason, throwable)
        repository.rollbackAddView(photocardId)
    }
}
package io.github.vladimirmi.photon.data.jobs.photocard

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import io.github.vladimirmi.photon.data.managers.extensions.JobPriority
import io.github.vladimirmi.photon.data.managers.extensions.cancelOrWaitConnection
import io.github.vladimirmi.photon.data.managers.extensions.logCancel
import io.github.vladimirmi.photon.data.repository.photocard.PhotocardJobRepository

/**
 * Created by Vladimir Mikhalev 18.07.2017.
 */

class PhotocardDeleteJob(private val photocardId: String,
                         private val repository: PhotocardJobRepository)
    : Job(Params(JobPriority.HIGH)
        .addTags(TAG + photocardId)
        .requireNetwork()
        .persist()) {

    companion object {
        const val TAG = "PhotocardDeleteJob"
    }

    override fun onAdded() {}

    override fun onRun() {
        repository.delete(photocardId).blockingGet()
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        logCancel(cancelReason, throwable)
        repository.rollbackDelete(photocardId)
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int) =
            cancelOrWaitConnection(throwable, runCount)
}
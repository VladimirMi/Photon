package io.github.vladimirmi.photon.data.jobs.photocard

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import io.github.vladimirmi.photon.data.managers.extensions.JobPriority
import io.github.vladimirmi.photon.data.managers.extensions.cancelOrWaitConnection
import io.github.vladimirmi.photon.data.managers.extensions.getPhotocard
import io.github.vladimirmi.photon.data.managers.extensions.logCancel
import io.github.vladimirmi.photon.di.DaggerService

/**
 * Created by Vladimir Mikhalev 21.07.2017.
 */

class PhotocardAddViewJob(private val photocardId: String) :
        Job(Params(JobPriority.HIGH)
                .addTags(TAG + photocardId)
                .requireNetwork()) {

    companion object {
        const val TAG = "PhotocardAddViewJob"
    }

    private val dataManager = DaggerService.appComponent.dataManager()

    override fun onAdded() {}

    override fun onRun() {
//        var error: Throwable? = null

        dataManager.addView(photocardId).blockingGet()

//        error?.let { throw it }
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        logCancel(cancelReason, throwable)
        rollback()
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int) =
            cancelOrWaitConnection(throwable, runCount)

    private fun rollback() {
        val photocard = dataManager.getPhotocard(photocardId)
        photocard.views--
        dataManager.save(photocard)
    }
}
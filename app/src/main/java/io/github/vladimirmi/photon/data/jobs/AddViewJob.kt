package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.AppConfig

/**
 * Created by Vladimir Mikhalev 21.07.2017.
 */

class AddViewJob(private val photocardId: String) :
        Job(Params(JobPriority.HIGH)
                .setGroupId(TAG)
                .requireNetwork()
                .persist()) {

    companion object {
        const val TAG = "AddViewJobTag"
    }

    override fun onRun() {
        val dataManager = DaggerService.appComponent.dataManager()
        var error: Throwable? = null

        dataManager.addView(photocardId)
                .blockingSubscribe({}, { error = it })

        error?.let { throw it }
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int): RetryConstraint {
        return RetryConstraint.createExponentialBackoff(runCount, AppConfig.INITIAL_BACK_OFF_IN_MS)
    }

    override fun onAdded() {
        val dataManager = DaggerService.appComponent.dataManager()
        val photocard = dataManager.getDetachedObjFromDb(Photocard::class.java, photocardId)!!
        dataManager.saveToDB(photocard.apply { views++ })
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        logCancel(cancelReason, throwable)
    }
}
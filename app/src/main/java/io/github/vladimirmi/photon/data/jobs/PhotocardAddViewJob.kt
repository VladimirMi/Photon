package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.JobGroup
import io.github.vladimirmi.photon.utils.JobPriority
import io.github.vladimirmi.photon.utils.cancelOrWait
import io.github.vladimirmi.photon.utils.logCancel

/**
 * Created by Vladimir Mikhalev 21.07.2017.
 */

class PhotocardAddViewJob(val photocardId: String,
                          val skipNetworkPart: Boolean = false,
                          val onlyNetworkPart: Boolean = false) :
        Job(Params(JobPriority.HIGH)
                .setGroupId(JobGroup.PHOTOCARD)
                .addTags(JobGroup.PHOTOCARD + photocardId)
                .requireNetwork()
                .persist()) {

    companion object {
        const val TAG = "PhotocardAddViewJob"
    }

    override fun onRun() {
        if (skipNetworkPart) return
        val dataManager = DaggerService.appComponent.dataManager()
        var error: Throwable? = null

        dataManager.addView(photocardId)
                .blockingSubscribe({}, { error = it })

        error?.let { throw it }
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int): RetryConstraint {
        return cancelOrWait(throwable, runCount)
    }

    override fun onAdded() {
        if (onlyNetworkPart) return
        val dataManager = DaggerService.appComponent.dataManager()
        val photocard = dataManager.getDetachedObjFromDb(Photocard::class.java, photocardId)!!
        dataManager.saveToDB(photocard.apply { views++ })
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        logCancel(cancelReason, throwable)
    }
}
package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import io.github.vladimirmi.photon.data.jobs.queue.JobTask
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.JobGroup
import io.github.vladimirmi.photon.utils.JobPriority
import io.github.vladimirmi.photon.utils.cancelOrWait
import io.github.vladimirmi.photon.utils.logCancel

/**
 * Created by Vladimir Mikhalev 21.07.2017.
 */

class PhotocardAddViewJob(photocardId: String) :
        Job(Params(JobPriority.LOW)
                .setGroupId(JobGroup.PHOTOCARD)
                .requireNetwork()
                .persist()), JobTask {

    companion object {
        const val TAG = "PhotocardAddViewJob"
    }

    override var entityId = photocardId
    override var parentEntityId
        get() = entityId
        set(value) {
            entityId = value
        }
    override val tag = TAG
    override val type = JobTask.Type.NORMAL

    override fun onQueued() {
        val dataManager = DaggerService.appComponent.dataManager()
        val photocard = dataManager.getDetachedObjFromDb(Photocard::class.java, id)!!
        dataManager.saveToDB(photocard.apply { views++ })
    }

    override fun onAdded() {}

    override fun onRun() {
        val dataManager = DaggerService.appComponent.dataManager()
        var error: Throwable? = null

        dataManager.addView(parentEntityId)
                .blockingSubscribe({}, { error = it })

        error?.let { throw it }
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        logCancel(cancelReason, throwable)
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int) =
            cancelOrWait(throwable, runCount)
}
package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.CancelReason
import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import io.github.vladimirmi.photon.data.jobs.queue.JobTask
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.*
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * Created by Vladimir Mikhalev 18.07.2017.
 */

class PhotocardDeleteJob(photocardId: String)
    : Job(Params(JobPriority.HIGH)
        .setSingleId(photocardId)
        .setGroupId(JobGroup.PHOTOCARD)
        .requireNetwork()
        .persist()), JobTask {

    companion object {
        const val TAG = "PhotocardDeleteJob"
    }

    override var entityId = photocardId
    override var parentEntityId = photocardId
    override val tag = TAG
    override val type = JobTask.Type.DELETE

    override fun onAdded() {}

    override fun onRun() {
        var error: Throwable? = null
        val dataManager = DaggerService.appComponent.dataManager()

        dataManager.deletePhotocard(parentEntityId)
                .blockingSubscribe({}, { error = it })

        error?.let { throw it }
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        logCancel(cancelReason, throwable)
        if (cancelReason == CancelReason.CANCELLED_VIA_SHOULD_RE_RUN) {
            updatePhotocard()
        }
    }

    private fun updatePhotocard() {
        val dataManager = DaggerService.appComponent.dataManager()

        dataManager.getPhotocardFromNet(parentEntityId, dataManager.getProfileId(), Date(0).toString())
                .doOnNext { dataManager.saveToDB(it) }
                .subscribeOn(Schedulers.io())
                .subscribeWith(ErrorObserver())
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int) =
            cancelOrWait(throwable, runCount)
}
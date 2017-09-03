package io.github.vladimirmi.photon.data.jobs.photocard

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import io.github.vladimirmi.photon.data.managers.extensions.JobPriority
import io.github.vladimirmi.photon.data.managers.extensions.cancelOrWaitConnection
import io.github.vladimirmi.photon.data.managers.extensions.getPhotocard
import io.github.vladimirmi.photon.data.managers.extensions.logCancel
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.di.DaggerService

/**
 * Created by Vladimir Mikhalev 18.07.2017.
 */

class PhotocardDeleteJob(private val photocardId: String)
    : Job(Params(JobPriority.HIGH)
        .addTags(TAG + photocardId)
        .requireNetwork()
        .persist()) {

    companion object {
        const val TAG = "PhotocardDeleteJob"
    }

    private val dataManager = DaggerService.appComponent.dataManager()
    private val cache = DaggerService.appComponent.cache()

    override fun onAdded() {}

    override fun onRun() {
        dataManager.deletePhotocard(photocardId).blockingGet()
        dataManager.removeFromDb(Photocard::class.java, photocardId)
        cache.removePhoto(photocardId)
    }


    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        logCancel(cancelReason, throwable)
//        if (cancelReason == CancelReason.CANCELLED_VIA_SHOULD_RE_RUN) {
        rollback()
//        }
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int) =
            cancelOrWaitConnection(throwable, runCount)

    private fun rollback() {
        val photocard = dataManager.getPhotocard(photocardId)
        photocard.active = true
        dataManager.save(photocard)
    }
}
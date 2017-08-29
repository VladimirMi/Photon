package io.github.vladimirmi.photon.data.jobs.photocard

import com.birbit.android.jobqueue.CancelReason
import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.*
import io.reactivex.schedulers.Schedulers

/**
 * Created by Vladimir Mikhalev 18.07.2017.
 */

class PhotocardDeleteJob(private val photocardId: String)
    : Job(Params(JobPriority.HIGH)
        .setSingleId(photocardId)
        .setGroupId(JobGroup.PHOTOCARD)
        .addTags(TAG)
        .requireNetwork()
        .persist()) {

    companion object {
        const val TAG = "PhotocardDeleteJob"
    }

    override fun onAdded() {}

    override fun onRun() {
        var error: Throwable? = null
        val dataManager = DaggerService.appComponent.dataManager()

        dataManager.deletePhotocard(photocardId)
                .doOnNext { dataManager.removeFromDb(Photocard::class.java, photocardId) }
                .blockingSubscribe({}, { error = it })

        error?.let { throw it }
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        logCancel(cancelReason, throwable)
        if (cancelReason == CancelReason.CANCELLED_VIA_SHOULD_RE_RUN) {
            updatePhotocard()
        }
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int) =
            cancelOrWaitConnection(throwable, runCount)

    private fun updatePhotocard() {
        val dataManager = DaggerService.appComponent.dataManager()

        dataManager.getPhotocardFromNet(photocardId, dataManager.getProfileId(), "0")
                .doOnNext { dataManager.saveFromNet(it) }
                .subscribeOn(Schedulers.io())
                .subscribeWith(ErrorObserver())
    }
}
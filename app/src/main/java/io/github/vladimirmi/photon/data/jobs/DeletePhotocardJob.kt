package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.CancelReason
import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.ErrorObserver
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * Created by Vladimir Mikhalev 18.07.2017.
 */

class DeletePhotocardJob(private val photocardId: String)
    : Job(Params(JobPriority.MID)
        .setSingleId(photocardId)
        .requireNetwork()
        .persist()) {

    override fun onAdded() {
        val dataManager = DaggerService.appComponent.dataManager()
        val cache = DaggerService.appComponent.cache()

        cache.removePhoto(photocardId)
        dataManager.removeFromDb(Photocard::class.java, photocardId)
    }

    override fun onRun() {
        var error: Throwable? = null
        val dataManager = DaggerService.appComponent.dataManager()

        dataManager.deletePhotocard(photocardId)
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

        dataManager.getPhotocardFromNet(photocardId, dataManager.getProfileId(), Date(0).toString())
                .doOnNext { dataManager.saveToDB(it) }
                .subscribeOn(Schedulers.io())
                .subscribeWith(ErrorObserver())
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int): RetryConstraint {
        return RetryConstraint.CANCEL
    }
}
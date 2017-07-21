package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.di.DaggerService
import io.reactivex.Observable

/**
 * Created by Vladimir Mikhalev 21.07.2017.
 */

class AddViewJob(id: String) :
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

        Observable.just(dataManager.getDetachedObjFromDb(Photocard::class.java, id))
                .doOnNext {
                    it.views++
                    dataManager.saveToDB(it)
                }
                .flatMap { dataManager.addView(id) }
                .blockingSubscribe({}, { error = it })

        error?.let { throw it }
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int): RetryConstraint {
        return RetryConstraint.CANCEL
    }

    override fun onAdded() {}

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        logCancel(cancelReason, throwable)
    }
}
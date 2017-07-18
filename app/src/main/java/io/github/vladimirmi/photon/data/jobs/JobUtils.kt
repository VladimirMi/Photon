package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.CancelReason
import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.JobManager
import com.birbit.android.jobqueue.callback.JobManagerCallback
import com.crashlytics.android.Crashlytics
import io.reactivex.Single
import io.reactivex.disposables.Disposables
import timber.log.Timber

/**
 * Created by Vladimir Mikhalev 25.06.2017.
 */

object JobPriority {
    const val LOW = 0
    const val MID = 500
    const val HIGH = 1000
}

open class EmptyJobCallback : JobManagerCallback {
    override fun onJobRun(job: Job, resultCode: Int) {}

    override fun onDone(job: Job) {}

    override fun onAfterJobRun(job: Job, resultCode: Int) {}

    override fun onJobCancelled(job: Job, byCancelRequest: Boolean, throwable: Throwable?) {}

    override fun onJobAdded(job: Job) {}
}

fun logCancel(cancelReason: Int, throwable: Throwable?) {
    val reason = when (cancelReason) {
        CancelReason.SINGLE_INSTANCE_ID_QUEUED -> "Cancel via: job with the same single id was already queued"
        CancelReason.REACHED_RETRY_LIMIT -> "Cancel via: reached retry limit"
        CancelReason.CANCELLED_WHILE_RUNNING -> "Cancel via: manual cancel"
        CancelReason.SINGLE_INSTANCE_WHILE_RUNNING -> "Cancel via: job with the same single id was queued while it running"
        CancelReason.CANCELLED_VIA_SHOULD_RE_RUN -> "Cancel via: exception while run and not rerun"
        CancelReason.REACHED_DEADLINE -> "Cancel via: hitting its deadline"
        else -> "Cancel via: unknown reason"
    }
    Timber.e(reason)
    Crashlytics.log(reason)
    Crashlytics.logException(throwable)
}

fun <T : Job> JobManager.singleResultFor(localJob: T): Single<Unit> {
    return Single.create { e ->
        val callback = object : EmptyJobCallback() {
            override fun onDone(job: Job) {
                if (!e.isDisposed && localJob.id == job.id) e.onSuccess(Unit)
            }

            override fun onJobCancelled(job: Job, byCancelRequest: Boolean, throwable: Throwable?) {
                Timber.e("onJobCancelled byCancelRequest $byCancelRequest ")
                Timber.e(throwable, throwable?.localizedMessage)
                if (!e.isDisposed && throwable != null && localJob.id == job.id) e.onError(throwable)
            }

            override fun onAfterJobRun(job: Job, resultCode: Int) {
                Timber.e("onAfterJobRun: $resultCode")
            }
        }

        addCallback(callback)
        e.setDisposable(Disposables.fromRunnable { removeCallback(callback) })
    }
}

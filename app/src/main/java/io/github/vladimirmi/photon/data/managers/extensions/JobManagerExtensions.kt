package io.github.vladimirmi.photon.data.managers.extensions

import com.birbit.android.jobqueue.CancelReason
import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.JobManager
import com.birbit.android.jobqueue.RetryConstraint
import com.birbit.android.jobqueue.callback.JobManagerCallback
import com.crashlytics.android.Crashlytics
import io.github.vladimirmi.photon.data.jobs.ChainJob
import io.github.vladimirmi.photon.data.managers.extensions.JobStatus.Status.*
import io.github.vladimirmi.photon.utils.AppConfig
import io.reactivex.Observable
import io.reactivex.disposables.Disposables
import timber.log.Timber
import java.net.SocketTimeoutException

/**
 * Created by Vladimir Mikhalev 03.09.2017.
 */

object JobPriority {
    const val LOW = 0
    const val MID = 500
    const val HIGH = 1000
}

object JobGroup {
    const val PROFILE = "PROFILE"
    const val ALBUM = "ALBUM"
    const val PHOTOCARD = "PHOTOCARD"
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

fun cancelOrWaitConnection(throwable: Throwable, runCount: Int): RetryConstraint {
    return if (throwable is SocketTimeoutException) {
        RetryConstraint.createExponentialBackoff(runCount, AppConfig.INITIAL_BACK_OFF_IN_MS)
    } else {
        RetryConstraint.CANCEL
    }
}

class JobStatus(val job: Job?, val status: Status) {
    enum class Status {ADDED, RUN, AFTER_RUN, DONE, QUEUED }

    fun isDone() = status == DONE
}

fun JobManager.observe(tag: String): Observable<JobStatus> {
    return Observable.create { e ->
        val callback = object : EmptyJobCallback() {
            override fun onDone(job: Job) {
                if (!e.isDisposed && job.tags?.contains(tag) == true) {
                    e.onNext(JobStatus(job, DONE))
                    e.onComplete()
                }
            }

            override fun onJobCancelled(job: Job, byCancelRequest: Boolean, throwable: Throwable?) {
                Timber.e(throwable, throwable?.localizedMessage)
                if (!e.isDisposed && job.tags?.contains(tag) == true) {
                    if (throwable != null) e.onError(throwable)
                }
            }

            override fun onAfterJobRun(job: Job, resultCode: Int) {
                if (!e.isDisposed && job.tags?.contains(tag) == true) e.onNext(JobStatus(job, AFTER_RUN))
            }

            override fun onJobAdded(job: Job) {
                if (!e.isDisposed && job.tags?.contains(tag) == true) e.onNext(JobStatus(job, ADDED))
            }

            override fun onJobRun(job: Job, resultCode: Int) {
                if (!e.isDisposed && job.tags?.contains(tag) == true) e.onNext(JobStatus(job, RUN))
            }
        }

        addCallback(callback)
        e.setDisposable(Disposables.fromRunnable { removeCallback(callback) })
        e.onNext(JobStatus(null, QUEUED))
    }
}

fun JobManager.addAndObserve(job: ChainJob): Observable<JobStatus> {
    addJob(job.getJob())
    return observe(job.newTag)
}
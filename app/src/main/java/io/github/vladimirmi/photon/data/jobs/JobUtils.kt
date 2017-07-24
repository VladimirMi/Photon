package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.*
import com.birbit.android.jobqueue.callback.JobManagerCallback
import com.crashlytics.android.Crashlytics
import io.reactivex.Observable
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
                if (!e.isDisposed && localJob.id == job.id) {
                    if (throwable != null) e.onError(throwable) else e.onSuccess(Unit)
                }
            }
        }

        addCallback(callback)
        e.setDisposable(Disposables.fromRunnable { removeCallback(callback) })
    }
}

enum class JobStatus {ADDED, RUN, AFTER_RUN }

fun <T : Job> JobManager.observableFor(localJob: T): Observable<JobStatus> {
    return Observable.create { e ->
        val callback = object : EmptyJobCallback() {
            override fun onDone(job: Job) {
                if (!e.isDisposed && localJob.id == job.id) e.onComplete()
            }

            override fun onJobCancelled(job: Job, byCancelRequest: Boolean, throwable: Throwable?) {
                Timber.e("onJobCancelled byCancelRequest $byCancelRequest ")
                Timber.e(throwable, throwable?.localizedMessage)
                if (!e.isDisposed && localJob.id == job.id) {
                    if (throwable != null) e.onError(throwable) else e.onComplete()
                }
            }

            override fun onAfterJobRun(job: Job, resultCode: Int) {
                if (!e.isDisposed && localJob.id == job.id) e.onNext(JobStatus.AFTER_RUN)
            }

            override fun onJobAdded(job: Job) {
                if (!e.isDisposed && localJob.id == job.id) e.onNext(JobStatus.ADDED)
            }

            override fun onJobRun(job: Job, resultCode: Int) {
                if (!e.isDisposed && localJob.id == job.id) e.onNext(JobStatus.RUN)
            }
        }

        addCallback(callback)
        e.setDisposable(Disposables.fromRunnable { removeCallback(callback) })
    }
}

fun JobManager.singleCancelJobs(constraint: TagConstraint, vararg tags: String)
        : Single<CancelResult> {

    return Single.create { e ->
        cancelJobsInBackground(CancelResult.AsyncCancelCallback {
            if (!e.isDisposed) e.onSuccess(it)
        }, constraint, *tags)
    }

}

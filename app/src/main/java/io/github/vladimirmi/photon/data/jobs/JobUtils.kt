package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.CancelReason
import com.birbit.android.jobqueue.Job
import com.crashlytics.android.Crashlytics

/**
 * Created by Vladimir Mikhalev 25.06.2017.
 */

object JobPriority {
    const val LOW = 0
    const val MID = 500
    const val HIGH = 1000
}

fun Job.logCancel(cancelReason: Int, throwable: Throwable?) {
    val reason = when (cancelReason) {
        CancelReason.SINGLE_INSTANCE_ID_QUEUED -> "Cancel via: job with the same single id was already queued"
        CancelReason.REACHED_RETRY_LIMIT -> "Cancel via: reached retry limit"
        CancelReason.CANCELLED_WHILE_RUNNING -> "Cancel via: manual cancel"
        CancelReason.SINGLE_INSTANCE_WHILE_RUNNING -> "Cancel via: job with the same single id was queued while it running"
        CancelReason.CANCELLED_VIA_SHOULD_RE_RUN -> "Cancel via: exception while run and not rerun"
        CancelReason.REACHED_DEADLINE -> "Cancel via: hitting its deadline"
        else -> "Cancel via: unknown reason"
    }
    Crashlytics.log(reason)
    Crashlytics.logException(throwable)
}

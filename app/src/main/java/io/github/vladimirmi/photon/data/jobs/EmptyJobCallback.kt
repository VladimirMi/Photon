package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.callback.JobManagerCallback

/**
 * Created by Vladimir Mikhalev 08.07.2017.
 */

open class EmptyJobCallback : JobManagerCallback {
    override fun onJobRun(job: Job, resultCode: Int) {}

    override fun onDone(job: Job) {}

    override fun onAfterJobRun(job: Job, resultCode: Int) {}

    override fun onJobCancelled(job: Job, byCancelRequest: Boolean, throwable: Throwable?) {}

    override fun onJobAdded(job: Job) {}
}
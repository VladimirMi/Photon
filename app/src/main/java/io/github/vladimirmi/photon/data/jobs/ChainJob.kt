package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.JobManager
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.TagConstraint
import io.github.vladimirmi.photon.data.managers.extensions.JobPriority
import io.github.vladimirmi.photon.data.managers.extensions.cancelOrWaitConnection
import io.github.vladimirmi.photon.data.managers.extensions.logCancel
import io.github.vladimirmi.photon.data.models.realm.isTemp

/**
 * Created by Vladimir Mikhalev 10.09.2017.
 */

abstract class ChainJob(tag: String, entityId: String)
    : Job(Params(JobPriority.HIGH)
        .addTags(tag + entityId)
        .requireNetwork()
        .persist()) {


    val tag = tag + entityId
    private val queue: ArrayList<ChainJob> = ArrayList()

    open val needCreate: String? = null
    open val needCancel: String? = null
    open val needReplace: String? = null


    fun getJob(jobManager: JobManager): Job? {
        val parent = needCreate?.let { getParent(it, jobManager) }
        if (parent != null) {
            if (parent.queue.removeAll { it.tag == needCancel }) return null
            parent.queue.removeAll { it.tag == needReplace }
            parent.queue.add(this)
        }
        return parent ?: this
    }

    private fun getParent(needCreate: String, jobManager: JobManager): ChainJob? {
        if (!needCreate.isTemp()) return null
        val cancelResult = jobManager.cancelJobs(TagConstraint.ANY, needCreate)
        val parent = cancelResult.cancelledJobs.firstOrNull()
        return parent as? ChainJob
    }

    override fun onRun() {}

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int) =
            cancelOrWaitConnection(throwable, runCount)

    override fun onAdded() {}

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        logCancel(cancelReason, throwable)
    }
}
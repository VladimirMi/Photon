package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import io.github.vladimirmi.photon.data.managers.extensions.JobPriority
import io.github.vladimirmi.photon.data.models.realm.Synchronizable
import io.github.vladimirmi.photon.data.repository.BaseEntityRepository
import io.realm.RealmObject

/**
 * Created by Vladimir Mikhalev 06.09.2017.
 */

class SyncCompleteJob<T : BaseEntityRepository>(private val repository: T,
                                                private val obj: Synchronizable)
    : Job(Params(JobPriority.HIGH)
        .addTags(TAG + obj.id)) {

    companion object {
        const val TAG = "SyncCompleteJob"
    }

    override fun onRun() {
        obj.sync = true
        repository.save(obj as RealmObject)
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int): RetryConstraint =
            RetryConstraint.CANCEL

    override fun onAdded() {}
    override fun onCancel(cancelReason: Int, throwable: Throwable?) {}
}
package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.Job
import io.github.vladimirmi.photon.data.models.realm.Synchronizable
import io.github.vladimirmi.photon.data.repository.BaseEntityRepository

/**
 * Created by Vladimir Mikhalev 30.08.2017.
 */

abstract class BaseJobsManager<T : Synchronizable, out R : BaseEntityRepository>
constructor(protected val repository: R) {

    fun nextJob(client: T): Job? {
        if (!client.active) return getDeleteJob(client)
        if (client.isTemp) return getCreateJob(client)
        return getJob(client, getFromServer(client.id)) ?:
                SyncCompleteJob(repository, client)
    }

    protected abstract fun getJob(client: T, server: T): Job?

    protected abstract fun getFromServer(id: String): T

    protected abstract fun getCreateJob(client: T): Job?

    protected abstract fun getDeleteJob(client: T): Job?
}

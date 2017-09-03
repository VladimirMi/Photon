package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.Job
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.realm.Synchronizable
import io.realm.RealmObject

/**
 * Created by Vladimir Mikhalev 30.08.2017.
 */

abstract class BaseJobsManager<T : Synchronizable>(protected val dataManager: DataManager) {

    private val runningJobs = HashSet<String>()

    fun completeJob(tag: String) {
        runningJobs.remove(tag)
    }

    abstract fun getFromServer(id: String): T

    fun nextJob(client: T): Job? {
        if (!client.active) return getDeleteJob(client.id)
        if (client.isTemp) return getCreateJob(client.id)
        val job = getJob(client, getFromServer(client.id))
        if (job != null) return job
        saveSync(client)
        return null
    }

    abstract fun getJob(client: T, server: T): Job?

    protected abstract fun getCreateJob(id: String): Job?

    protected abstract fun getDeleteJob(id: String): Job?

    protected fun canRun(tag: String): Boolean = runningJobs.add(tag)

    private fun saveSync(obj: Synchronizable) {
        obj.sync = true
        dataManager.save(obj as RealmObject)
    }
}

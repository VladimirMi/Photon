package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.Job
import io.github.vladimirmi.photon.data.jobs.profile.ProfileEditJob
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.data.models.req.ProfileEditReq

/**
 * Created by Vladimir Mikhalev 29.08.2017.
 */

class ProfileJobsManager(dataManager: DataManager) : BaseJobsManager<User>(dataManager) {

    override fun getFromServer(id: String): User =
            dataManager.getUserFromNet(id, force = true).blockingFirst()

    override fun getJob(client: User, server: User): Job? = getEditJob(client, server)

    override fun getCreateJob(id: String): Job? = null

    override fun getDeleteJob(id: String): Job? = null

    private fun getEditJob(client: User, server: User): Job? =
            if (isEdited(server, client) && canRun(ProfileEditJob.TAG + client.id)) {
                ProfileEditJob(client.id)
            } else null

    private fun isEdited(client: User, server: User): Boolean =
            ProfileEditReq.from(client) != ProfileEditReq.from(server)
}


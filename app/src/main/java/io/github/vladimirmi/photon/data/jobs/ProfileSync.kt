package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.JobManager
import io.github.vladimirmi.photon.data.jobs.profile.ProfileEditJob
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.realm.User
import timber.log.Timber

/**
 * Created by Vladimir Mikhalev 29.08.2017.
 */

class ProfileSync(private val jobManager: JobManager,
                  private val dataManager: DataManager) {


    fun create(localProfile: User) {
        throw UnsupportedOperationException()
    }

    fun delete(localProfile: User) {
        throw UnsupportedOperationException()
    }

    fun edit(localProfile: User) {
        Timber.e("edit: ")
        saveSync(localProfile)
        jobManager.addJobInBackground(ProfileEditJob())
    }

    private fun saveSync(localProfile: User) {
        localProfile.sync = true
        dataManager.save(localProfile)
    }
}
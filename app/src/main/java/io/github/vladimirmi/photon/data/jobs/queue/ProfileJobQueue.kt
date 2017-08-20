package io.github.vladimirmi.photon.data.jobs.queue

import io.github.vladimirmi.photon.data.jobs.ProfileEditJob
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.data.models.req.EditProfileReq
import io.github.vladimirmi.photon.utils.JobStatus
import io.github.vladimirmi.photon.utils.ioToMain
import io.reactivex.Observable

/**
 * Created by Vladimir Mikhalev 25.07.2017.
 */

class ProfileJobQueue(private val jobQueue: JobQueue,
                      private val dataManager: DataManager) {

    fun queueEditJob(request: EditProfileReq): Observable<JobStatus> {
        return Observable.just {
            val profile = dataManager.getDetachedObjFromDb(User::class.java, dataManager.getProfileId())!!

            profile.apply {
                login = request.login
                name = request.name
                avatar = request.avatar
            }
            dataManager.saveToDB(profile)
        }
                .flatMap { jobQueue.add(ProfileEditJob(request)) }
                .ioToMain()
    }
}
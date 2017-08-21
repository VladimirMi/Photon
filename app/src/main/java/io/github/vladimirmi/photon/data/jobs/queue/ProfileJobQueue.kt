package io.github.vladimirmi.photon.data.jobs.queue

import io.github.vladimirmi.photon.data.jobs.ProfileEditJob
import io.github.vladimirmi.photon.data.models.req.EditProfileReq
import io.github.vladimirmi.photon.utils.JobStatus
import io.reactivex.Observable

/**
 * Created by Vladimir Mikhalev 25.07.2017.
 */

class ProfileJobQueue(private val jobQueue: JobQueue) {

    fun queueEditJob(request: EditProfileReq): Observable<JobStatus> =
            Observable.defer { jobQueue.add(ProfileEditJob(request)) }
}
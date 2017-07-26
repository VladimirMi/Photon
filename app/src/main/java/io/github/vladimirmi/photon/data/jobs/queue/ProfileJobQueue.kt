package io.github.vladimirmi.photon.data.jobs.queue

import com.birbit.android.jobqueue.JobManager
import com.birbit.android.jobqueue.TagConstraint
import io.github.vladimirmi.photon.data.jobs.ProfileEditJob
import io.github.vladimirmi.photon.data.models.req.EditProfileReq
import io.github.vladimirmi.photon.utils.JobStatus
import io.github.vladimirmi.photon.utils.observableFor
import io.github.vladimirmi.photon.utils.singleCancelJobs
import io.reactivex.Observable

/**
 * Created by Vladimir Mikhalev 25.07.2017.
 */

class ProfileJobQueue(private val jobManager: JobManager) {

    fun queueEditJob(request: EditProfileReq): Observable<JobStatus> {
        return jobManager.singleCancelJobs(TagConstraint.ANY, ProfileEditJob.TAG)
                .flatMapObservable {
                    val job = ProfileEditJob(request)
                    jobManager.addJobInBackground(job)
                    jobManager.observableFor(job)
                }
    }
}
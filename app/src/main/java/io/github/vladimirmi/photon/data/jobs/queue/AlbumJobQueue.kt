package io.github.vladimirmi.photon.data.jobs.queue

import io.github.vladimirmi.photon.data.jobs.AlbumCreateJob
import io.github.vladimirmi.photon.data.jobs.AlbumDeleteJob
import io.github.vladimirmi.photon.data.jobs.AlbumEditJob
import io.github.vladimirmi.photon.data.models.req.AlbumEditReq
import io.github.vladimirmi.photon.data.models.req.NewAlbumReq
import io.github.vladimirmi.photon.utils.JobStatus
import io.reactivex.Observable

/**
 * Created by Vladimir Mikhalev 25.07.2017.
 */

class AlbumJobQueue(private val jobQueue: JobQueue) {

    fun queueCreateJob(request: NewAlbumReq): Observable<JobStatus> =
            Observable.defer { jobQueue.add(AlbumCreateJob(request)) }

    fun queueDeleteJob(id: String): Observable<JobStatus> =
            Observable.defer { jobQueue.add(AlbumDeleteJob(id)) }

    fun queueEditJob(request: AlbumEditReq): Observable<JobStatus> =
            Observable.defer { jobQueue.add(AlbumEditJob(request)) }
}

package io.github.vladimirmi.photon.data.jobs.queue

import com.birbit.android.jobqueue.JobManager
import com.birbit.android.jobqueue.TagConstraint
import io.github.vladimirmi.photon.data.jobs.AlbumCreateJob
import io.github.vladimirmi.photon.data.jobs.AlbumDeleteJob
import io.github.vladimirmi.photon.data.jobs.AlbumEditJob
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.req.EditAlbumReq
import io.github.vladimirmi.photon.data.models.req.NewAlbumReq
import io.github.vladimirmi.photon.utils.*
import io.reactivex.Observable

/**
 * Created by Vladimir Mikhalev 25.07.2017.
 */

class AlbumJobQueue(private val jobManager: JobManager,
                    private val dataManager: DataManager,
                    private val photocardJobQueue: PhotocardJobQueue) {

    fun queueCreateJob(request: NewAlbumReq): Observable<JobStatus> {
        val job = AlbumCreateJob(request)
        jobManager.addJobInBackground(job)
        jobManager.observablePayloadFor(job)
                .doOnNext { payload -> syncAlbumJobs(request.id, payload.value as String) }
                .subscribeWith(ErrorObserver())
        return jobManager.observableFor(job)
    }

    fun queueDeleteJob(id: String): Observable<JobStatus> {
        val job = if (albumIsTemp(id)) {
            AlbumDeleteJob(id, skipNetworkPart = true)
        } else {
            AlbumDeleteJob(id, skipNetworkPart = false)
        }
        val album = dataManager.getDetachedObjFromDb(Album::class.java, id)!!
        album.photocards.forEach {
            photocardJobQueue.queueDeleteJob(it.id)
        }
        return jobManager.singleCancelJobs(TagConstraint.ANY, JobGroup.ALBUM + id)
                .flatMapObservable {
                    jobManager.addJobInBackground(job)
                    jobManager.observableFor(job)
                }
    }

    fun queueEditJob(request: EditAlbumReq): Observable<JobStatus> {
        val job = if (albumIsTemp(request.id)) {
            AlbumEditJob(request, skipNetworkPart = true)
        } else {
            AlbumEditJob(request, skipNetworkPart = false)
        }

        return jobManager.singleCancelJobs(TagConstraint.ANY, AlbumEditJob.TAG + request.id)
                .flatMapObservable {
                    jobManager.addJobInBackground(job)
                    jobManager.observableFor(job)
                }
    }

    private fun syncAlbumJobs(tempId: String, newId: String) {
        jobManager.singleCancelJobs(TagConstraint.ANY, JobGroup.ALBUM + tempId)
                .doOnSuccess { cancelResult ->
                    cancelResult.cancelledJobs.forEach {
                        handleCancelledTags(it.tags!!, tempId, newId)
                    }
                }.subscribeWith(ErrorSingleObserver())
    }

    private fun handleCancelledTags(tags: Set<String>, tempId: String, newId: String) {
        val tempAlbum = dataManager.getDetachedObjFromDb(Album::class.java, tempId)!!
        when {
            tags.contains(AlbumEditJob.TAG + tempId) -> {
                val request = EditAlbumReq(newId, tempAlbum.title, tempAlbum.description)
                jobManager.addJobInBackground(AlbumEditJob(request, skipNetworkPart = false))
            }
        }
    }

    private fun albumIsTemp(id: String) = id.startsWith(Album.TEMP)
}

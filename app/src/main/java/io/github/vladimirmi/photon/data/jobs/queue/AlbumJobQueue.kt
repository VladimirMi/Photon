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
import timber.log.Timber

/**
 * Created by Vladimir Mikhalev 25.07.2017.
 */

class AlbumJobQueue(private val jobManager: JobManager,
                    private val dataManager: DataManager,
                    private val photocardJobQueue: PhotocardJobQueue) {

    fun queueCreateJob(request: NewAlbumReq): Observable<JobStatus> {
        Timber.e("queueCreateJob: ")
        val job = AlbumCreateJob(request)
        jobManager.addJobInBackground(job)
        syncAlbumJobsAfter(job)
        return jobManager.observableFor(job)
    }

    fun queueDeleteJob(id: String): Observable<JobStatus> {
        val album = dataManager.getDetachedObjFromDb(Album::class.java, id)!!
        album.photocards.forEach {
            photocardJobQueue.queueDeleteJob(it.id)
        }
        return jobManager.singleCancelJobs(TagConstraint.ANY, JobGroup.ALBUM + id)
                .map {
                    if (albumIsTemp(id)) {
                        Timber.e("queueDeleteJob: ")
                        AlbumDeleteJob(id, skipNetworkPart = true)
                    } else {
                        Timber.e("queueDeleteJob: net")
                        AlbumDeleteJob(id)
                    }
                }
                .flatMapObservable {
                    jobManager.addJobInBackground(it)
                    jobManager.observableFor(it)
                }
    }

    fun queueEditJob(request: EditAlbumReq): Observable<JobStatus> {
        return jobManager.singleCancelJobs(TagConstraint.ANY, AlbumEditJob.TAG + request.id)
                .map {
                    if (albumIsTemp(request.id) || it.cancelledJobs.isNotEmpty()) {
                        Timber.e("queueEditJob: ")
                        AlbumEditJob(request, skipNetworkPart = true)
                    } else {
                        Timber.e("queueEditJob: net")
                        AlbumEditJob(request)
                    }
                }
                .flatMapObservable {
                    jobManager.addJobInBackground(it)
                    jobManager.observableFor(it)
                }
    }

    private fun syncAlbumJobsAfter(job: AlbumCreateJob) {
        jobManager.observablePayloadFor(job)
                .flatMap { payload ->
                    Timber.e("syncAlbumJobsAfter: ${payload.value as String}")
                    substituteTempJobs(job.request.id, payload.value as String)
                }
                .doOnComplete { job.removeTempAlbum() }
                .subscribeWith(ErrorObserver())
    }

    private fun substituteTempJobs(tempId: String, newId: String): Observable<Unit> {
        return jobManager.singleCancelJobs(TagConstraint.ANY, JobGroup.ALBUM + tempId)
                .flatMapObservable {
                    Timber.e("substituteTempJobs: ${it.cancelledJobs}")
                    Timber.e("substituteTempJobs: ${it.failedToCancel}")
                    Observable.fromIterable(it.cancelledJobs + it.failedToCancel)
                }
                .map { it.tags?.let { handleCancelledTags(it, tempId, newId) } ?: Unit }
    }

    private fun handleCancelledTags(tags: Set<String>, tempId: String, newId: String) {
        Timber.e("handleCancelledTags: $tags, $tempId, $newId")
        val tempAlbum = dataManager.getDetachedObjFromDb(Album::class.java, tempId)!!
        when {
            tags.contains(AlbumEditJob.TAG + tempId) -> {
                val request = EditAlbumReq(newId, tempAlbum.title, tempAlbum.description)
                queueEditJob(request).subscribeWith(ErrorObserver<JobStatus>())
            }
        }
    }

    private fun albumIsTemp(id: String) = id.startsWith(Album.TEMP)
}

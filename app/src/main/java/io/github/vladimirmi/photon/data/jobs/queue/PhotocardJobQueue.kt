package io.github.vladimirmi.photon.data.jobs.queue

import com.birbit.android.jobqueue.JobManager
import com.birbit.android.jobqueue.TagConstraint
import io.github.vladimirmi.photon.data.jobs.*
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.utils.*
import io.reactivex.Observable
import timber.log.Timber

/**
 * Created by Vladimir Mikhalev 25.07.2017.
 */

class PhotocardJobQueue(private val jobManager: JobManager, private val dataManager: DataManager) {


    fun queueCreateJob(id: String, albumId: String): Observable<JobStatus> {
        Timber.e("queueCreateJob: ")
        val job = PhotocardCreateJob(id, albumId)
//        jobManager.addJobInBackground(job)
        syncPhotocardJobsAfter(job)
        return jobManager.observableFor(job)
    }

    fun queueDeleteJob(id: String): Observable<JobStatus> {
        return jobManager.singleCancelJobs(TagConstraint.ANY, JobGroup.PHOTOCARD + id)
                .map {
                    if (photocardIsTemp(id)) {
                        Timber.e("queueDeleteJob: ")
                        PhotocardDeleteJob(id, skipNetworkPart = true)
                    } else {
                        Timber.e("queueDeleteJob: net")
                        PhotocardDeleteJob(id)
                    }
                }
                .flatMapObservable {
                    jobManager.addJobInBackground(it)
                    jobManager.observableFor(it)
                }
    }


    fun queueAddViewJob(id: String): Observable<JobStatus> {
        val job = if (photocardIsTemp(id)) {
            Timber.e("queueAddViewJob: ")
            PhotocardAddViewJob(id, skipNetworkPart = true)
        } else {
            Timber.e("queueAddViewJob: net")
            PhotocardAddViewJob(id)
        }
        jobManager.addJobInBackground(job)
        return jobManager.observableFor(job)
    }

    fun queueAddToFavoriteJob(id: String): Observable<JobStatus> {
        val favAlbumId = dataManager.getUserFavAlbumId()
        return jobManager.singleCancelJobs(TagConstraint.ANY, PhotocardDeleteFromFavoriteJob.TAG + id)
                .map {
                    if (photocardIsTemp(id) || it.cancelledJobs.isNotEmpty()) {
                        Timber.e("queueAddToFavoriteJob: ")
                        PhotocardAddToFavoriteJob(id, favAlbumId, skipNetworkPart = true)
                    } else {
                        Timber.e("queueAddToFavoriteJob: net")
                        PhotocardAddToFavoriteJob(id, favAlbumId)
                    }
                }
                .flatMapObservable {
                    jobManager.addJobInBackground(it)
                    jobManager.observableFor(it)
                }
    }

    fun queueDeleteFromFavoriteJob(id: String): Observable<JobStatus> {
        val favAlbumId = dataManager.getUserFavAlbumId()

        return jobManager.singleCancelJobs(TagConstraint.ANY, PhotocardAddToFavoriteJob.TAG + id)
                .map {
                    if (photocardIsTemp(id) || it.cancelledJobs.isNotEmpty()) {
                        Timber.e("queueDeleteFromFavoriteJob: ")
                        PhotocardDeleteFromFavoriteJob(id, favAlbumId, skipNetworkPart = true)
                    } else {
                        Timber.e("queueDeleteFromFavoriteJob: net")
                        PhotocardDeleteFromFavoriteJob(id, favAlbumId)
                    }
                }
                .flatMapObservable {
                    jobManager.addJobInBackground(it)
                    jobManager.observableFor(it)
                }
    }

    private fun syncPhotocardJobsAfter(job: PhotocardCreateJob) {
        jobManager.observablePayloadFor(job)
                .flatMap { payload ->
                    Timber.e("syncPhotocardJobsAfter: ${payload.value as String}")
                    substituteTempJobs(job.photocardId, payload.value as String)
                }
                .subscribeWith(ErrorObserver())
    }

    private fun substituteTempJobs(tempId: String, newId: String): Observable<Unit> {
        return jobManager.singleCancelJobs(TagConstraint.ANY, JobGroup.PHOTOCARD + tempId)
                .flatMapObservable {
                    Timber.e("substituteTempJobs: ${it.cancelledJobs}")
                    Timber.e("substituteTempJobs: ${it.failedToCancel}")
                    Observable.fromIterable(it.cancelledJobs + it.failedToCancel)
                }
                .map { it.tags?.let { handleCancelledTags(it, tempId, newId) } ?: Unit }
    }


    private fun handleCancelledTags(tags: Set<String>, tempId: String, newId: String) {
        Timber.e("handleCancelledTags: $tags, $tempId, $newId")
        when {
            tags.contains(PhotocardAddViewJob.TAG + tempId) -> queueAddViewJob(newId)
                    .subscribeWith(ErrorObserver<JobStatus>())

            tags.contains(PhotocardAddToFavoriteJob.TAG + tempId) -> queueAddToFavoriteJob(newId)
                    .subscribeWith(ErrorObserver<JobStatus>())

            tags.contains(PhotocardDeleteFromFavoriteJob.TAG + tempId) -> queueDeleteFromFavoriteJob(newId)
                    .subscribeWith(ErrorObserver<JobStatus>())
        }
    }

    private fun photocardIsTemp(id: String) = id.startsWith(Photocard.TEMP)
}

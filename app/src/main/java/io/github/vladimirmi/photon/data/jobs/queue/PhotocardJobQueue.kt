package io.github.vladimirmi.photon.data.jobs.queue

import io.github.vladimirmi.photon.data.jobs.*
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.JobStatus
import io.reactivex.Observable

/**
 * Created by Vladimir Mikhalev 25.07.2017.
 */

class PhotocardJobQueue(private val jobQueue: JobQueue) {

    fun queueCreateJob(photocard: Photocard, albumId: String): Observable<JobStatus> {
        return Observable.fromCallable {
            //todo move to onQueued
            val dataManager = DaggerService.appComponent.dataManager()
            val album = dataManager.getDetachedObjFromDb(Album::class.java, albumId)!!
            album.photocards.add(photocard)
            dataManager.saveToDB(album)
        }
                .flatMap { jobQueue.add(PhotocardCreateJob(photocard.id, albumId)) }
    }

    fun queueDeleteJob(id: String): Observable<JobStatus> =
            Observable.defer { jobQueue.add(PhotocardDeleteJob(id)) }


    fun queueAddViewJob(id: String): Observable<JobStatus> =
            Observable.defer { jobQueue.add(PhotocardAddViewJob(id)) }


    fun queueAddToFavoriteJob(id: String): Observable<JobStatus> =
            Observable.defer { jobQueue.add(PhotocardAddToFavoriteJob(id)) }


    fun queueDeleteFromFavoriteJob(id: String): Observable<JobStatus> =
            Observable.defer { jobQueue.add(PhotocardDeleteFromFavoriteJob(id)) }
}

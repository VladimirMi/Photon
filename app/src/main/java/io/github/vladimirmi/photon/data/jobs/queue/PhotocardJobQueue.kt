package io.github.vladimirmi.photon.data.jobs.queue

import io.github.vladimirmi.photon.data.jobs.*
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.JobStatus
import io.github.vladimirmi.photon.utils.ioToMain
import io.reactivex.Observable

/**
 * Created by Vladimir Mikhalev 25.07.2017.
 */

class PhotocardJobQueue(private val jobQueue: JobQueue,
                        private val dataManager: DataManager) {

    fun queueCreateJob(photocard: Photocard, albumId: String): Observable<JobStatus> {
        return Observable.just {
            val album = dataManager.getDetachedObjFromDb(Album::class.java, albumId)!!
            album.photocards.add(photocard)
            dataManager.saveToDB(album)
        }
                .flatMap { jobQueue.add(PhotocardCreateJob(photocard.id, albumId)) }
                .ioToMain()
    }

    fun queueDeleteJob(id: String): Observable<JobStatus> {
        return Observable.just {
            val cache = DaggerService.appComponent.cache()
            cache.removePhoto(id)
            dataManager.removeFromDb(Photocard::class.java, id)
        }
                .flatMap { jobQueue.add(PhotocardDeleteJob(id)) }
                .ioToMain()
    }


    fun queueAddViewJob(id: String): Observable<JobStatus> {
        return Observable.just {
            val photocard = dataManager.getDetachedObjFromDb(Photocard::class.java, id)!!
            dataManager.saveToDB(photocard.apply { views++ })
        }
                .flatMap { jobQueue.add(PhotocardAddViewJob(id)) }
                .ioToMain()
    }

    fun queueAddToFavoriteJob(id: String): Observable<JobStatus> {
        return Observable.just {
            val favAlbumId = dataManager.getUserFavAlbumId()
            val album = dataManager.getDetachedObjFromDb(Album::class.java, favAlbumId)!!
            val photocard = dataManager.getDetachedObjFromDb(Photocard::class.java, id)!!
            dataManager.saveToDB(album.apply { photocards.add(photocard) })
        }
                .flatMap { jobQueue.add(PhotocardAddToFavoriteJob(id)) }
                .ioToMain()
    }

    fun queueDeleteFromFavoriteJob(id: String): Observable<JobStatus> {
        return Observable.just {
            val favAlbumId = dataManager.getUserFavAlbumId()
            val album = dataManager.getDetachedObjFromDb(Album::class.java, favAlbumId)!!
            dataManager.saveToDB(album.apply { photocards.removeAll { it.id == id } })
        }
                .flatMap { jobQueue.add(PhotocardDeleteFromFavoriteJob(id)) }
                .ioToMain()
    }
}

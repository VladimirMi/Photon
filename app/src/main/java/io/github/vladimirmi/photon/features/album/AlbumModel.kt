package io.github.vladimirmi.photon.features.album

import io.github.vladimirmi.photon.data.jobs.queue.AlbumJobQueue
import io.github.vladimirmi.photon.data.jobs.queue.PhotocardJobQueue
import io.github.vladimirmi.photon.data.managers.Cache
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.dto.PhotocardDto
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.req.AlbumEditReq
import io.github.vladimirmi.photon.utils.*
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * Created by Vladimir Mikhalev 19.06.2017.
 */

class AlbumModel(val dataManager: DataManager,
                 val photocardJobQueue: PhotocardJobQueue,
                 val albumJobQueue: AlbumJobQueue,
                 val cache: Cache) : IAlbumModel {

    override fun getAlbum(id: String): Observable<AlbumDto> {
        updateAlbum(id)
        val album = dataManager.getObjectFromDb(Album::class.java, id)
                .flatMap { justOrEmpty(cache.cacheAlbum(it)) }
                .ioToMain()

        return Observable.merge(justOrEmpty(cache.album(id)), album)
    }

    private fun updateAlbum(id: String) {
        dataManager.isNetworkAvailable()
                .filter { it }
                .flatMap { Observable.just(dataManager.getDetachedObjFromDb(Album::class.java, id)?.updated ?: Date(0)) }
                .flatMap { dataManager.getAlbumFromNet(id, getUpdated(it)) }
                .doOnNext { dataManager.saveToDB(it) }
                .subscribeOn(Schedulers.io())
                .subscribeWith(ErrorObserver())
    }

    override fun getProfileId() = dataManager.getProfileId()

    override fun editAlbum(albumReq: AlbumEditReq): Observable<JobStatus> {
        return albumJobQueue.queueEditJob(albumReq)
                .ioToMain()
    }

    override fun deleteAlbum(albumId: String): Observable<JobStatus> {
        return albumJobQueue.queueDeleteJob(albumId)
                .ioToMain()
    }

    override fun removePhotos(photosForDelete: List<PhotocardDto>, album: AlbumDto): Single<Unit> {
        return removePhotosById(photosForDelete.map { it.id }, album.isFavorite)
                .ioToMain()
    }

    private fun removePhotosById(photosForDelete: List<String>,
                                 isFavorite: Boolean = false): Single<Unit> {
        if (photosForDelete.isEmpty()) return Single.just(Unit)

        return Observable.fromIterable(photosForDelete)
                .flatMap {
                    if (isFavorite) {
                        photocardJobQueue.queueDeleteFromFavoriteJob(it)
                    } else {
                        photocardJobQueue.queueDeleteJob(it)
                    }
                }.unit()
                .lastOrError()
    }
}
package io.github.vladimirmi.photon.features.album

import com.birbit.android.jobqueue.JobManager
import com.birbit.android.jobqueue.TagConstraint
import io.github.vladimirmi.photon.data.jobs.DeleteAlbumJob
import io.github.vladimirmi.photon.data.jobs.DeletePhotocardJob
import io.github.vladimirmi.photon.data.jobs.singleCancelJobs
import io.github.vladimirmi.photon.data.managers.Cache
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.EditAlbumReq
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.dto.PhotocardDto
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.utils.Constants
import io.github.vladimirmi.photon.utils.ioToMain
import io.github.vladimirmi.photon.utils.justOrEmpty
import io.github.vladimirmi.photon.utils.unit
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*

/**
 * Created by Vladimir Mikhalev 19.06.2017.
 */

class AlbumModel(val dataManager: DataManager, val jobManager: JobManager, val cache: Cache) : IAlbumModel {

    override fun getAlbum(id: String): Observable<AlbumDto> {
        //todo update album

        val album = dataManager.getObjectFromDb(Album::class.java, id)
                .map { cache.cacheAlbum(it) }
                .flatMap { justOrEmpty(cache.album(id)) }
                .ioToMain()

        return Observable.merge(justOrEmpty(cache.album(id)), album)

    }

    override fun getProfileId() = dataManager.getProfileId()

    override fun editAlbum(albumReq: EditAlbumReq): Observable<Unit> {
        return dataManager.editAlbum(albumReq)
                .map { dataManager.saveToDB(it) }
    }

    override fun deleteAlbum(albumId: String): Single<Unit> {
        val album = dataManager.getDetachedObjFromDb(Album::class.java, albumId)!!

        return cancelCreateOrRemovePhotos(album.photocards.map { it.id })
                .flatMap { jobManager.singleCancelJobs(TagConstraint.ALL, Constants.CREATE_ALBUM_JOB_TAG + albumId) }
                .map { cancelResult ->
                    if (cancelResult.cancelledJobs.isNotEmpty()) {
                        cache.removeAlbum(albumId)
                        dataManager.removeFromDb(Album::class.java, albumId)
                    } else {
                        jobManager.addJobInBackground(DeleteAlbumJob(albumId))
                    }
                }
                .ioToMain()
    }

    override fun removePhotos(photosForDelete: List<PhotocardDto>, album: AlbumDto): Observable<Unit> {
        if (album.isFavorite) {
            return removeFromFavorite(photosForDelete, album)
        }
        return cancelCreateOrRemovePhotos(photosForDelete.map { it.id })
                .toObservable()
                .unit()
    }

    private fun cancelCreateOrRemovePhotos(photocardsId: List<String>): Single<Unit> {
        if (photocardsId.isEmpty()) return Single.just(Unit)
        val createPhotoJobTags = photocardsId.mapTo(ArrayList()) { Constants.CREATE_PHOTOCART_JOB_TAG + it }

        return jobManager.singleCancelJobs(TagConstraint.ANY, *createPhotoJobTags.toTypedArray())
                .doOnSuccess { it.cancelledJobs.forEach { it.tags?.forEach { createPhotoJobTags.remove(it) } } }
                .flatMapObservable { Observable.fromIterable(createPhotoJobTags) }
                .map { jobManager.addJobInBackground(DeletePhotocardJob(it.removePrefix(Constants.CREATE_PHOTOCART_JOB_TAG))) }
                .last(Unit)
    }

    private fun removeFromFavorite(photosForDelete: List<PhotocardDto>, album: AlbumDto): Observable<Unit> {
        val albumRealm = dataManager.getDetachedObjFromDb(Album::class.java, album.id)!!
        return Observable.fromIterable(photosForDelete)
                .flatMap { photocard ->
                    dataManager.removeFromFavorite(photocard.id)
                            .doOnComplete { albumRealm.photocards.removeAll { it.id == photocard.id } }
                }.unit()
                .doOnComplete { dataManager.saveToDB(albumRealm) }
    }
}
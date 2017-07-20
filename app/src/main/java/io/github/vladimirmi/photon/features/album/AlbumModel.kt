package io.github.vladimirmi.photon.features.album

import com.birbit.android.jobqueue.JobManager
import com.birbit.android.jobqueue.TagConstraint
import io.github.vladimirmi.photon.data.jobs.*
import io.github.vladimirmi.photon.data.managers.Cache
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.EditAlbumReq
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.dto.PhotocardDto
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.utils.ErrorObserver
import io.github.vladimirmi.photon.utils.ioToMain
import io.github.vladimirmi.photon.utils.justOrEmpty
import io.github.vladimirmi.photon.utils.unit
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * Created by Vladimir Mikhalev 19.06.2017.
 */

class AlbumModel(val dataManager: DataManager, val jobManager: JobManager, val cache: Cache) : IAlbumModel {

    override fun getAlbum(id: String): Observable<AlbumDto> {
        updateAlbum(id)
        val album = dataManager.getObjectFromDb(Album::class.java, id)
                .flatMap { justOrEmpty(cache.cacheAlbum(it)) }
                .ioToMain()

        return Observable.merge(justOrEmpty(cache.album(id)), album)

    }

    private fun updateAlbum(id: String) {
        Observable.just(dataManager.getDetachedObjFromDb(Album::class.java, id))
                .flatMap { dataManager.getAlbumFromNet(it.id, getUpdated(it).toString()) }
                .doOnNext { dataManager.saveToDB(it) }
                .subscribeOn(Schedulers.io())
                .subscribeWith(ErrorObserver())
    }

    override fun getProfileId() = dataManager.getProfileId()

    override fun editAlbum(albumReq: EditAlbumReq): Single<Unit> {
        val job = EditAlbumJob(albumReq)
        return Single.just(dataManager.getDetachedObjFromDb(Album::class.java, albumReq.id))
                .doOnSuccess { album ->
                    album.apply { title = albumReq.title; description = albumReq.description }
                    dataManager.saveToDB(album)
                }
                .flatMap { jobManager.singleCancelJobs(TagConstraint.ANY, job.tag) }
                .doOnSuccess { jobManager.addJobInBackground(job) }
                .flatMap { jobManager.singleResultFor(job) }
                .ioToMain()
    }

    override fun deleteAlbum(albumId: String): Single<Unit> {
        val album = dataManager.getDetachedObjFromDb(Album::class.java, albumId)!!

        return cancelCreateOrRemovePhotos(album.photocards.map { it.id })
                .flatMap { jobManager.singleCancelJobs(TagConstraint.ANY, CreateAlbumJob.TAG + albumId) }
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

    override fun removePhotos(photosForDelete: List<PhotocardDto>, album: AlbumDto): Single<Unit> {
        if (album.isFavorite) {
            return removeFromFavorite(photosForDelete, album).lastOrError() //todo refactor to job
        }
        return cancelCreateOrRemovePhotos(photosForDelete.map { it.id })
    }

    private fun cancelCreateOrRemovePhotos(photocardsId: List<String>): Single<Unit> {
        if (photocardsId.isEmpty()) return Single.just(Unit)
        val createPhotoJobTags = photocardsId.mapTo(ArrayList()) { CreatePhotoJob.TAG + it }

        return jobManager.singleCancelJobs(TagConstraint.ANY, *createPhotoJobTags.toTypedArray())
                .doOnSuccess { it.cancelledJobs.forEach { it.tags?.forEach { createPhotoJobTags.remove(it) } } }
                .flatMapObservable { Observable.fromIterable(createPhotoJobTags) }
                .map { jobManager.addJobInBackground(DeletePhotocardJob(it.removePrefix(CreatePhotoJob.TAG))) }
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
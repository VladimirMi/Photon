package io.github.vladimirmi.photon.features.album

import com.birbit.android.jobqueue.JobManager
import com.birbit.android.jobqueue.TagConstraint
import io.github.vladimirmi.photon.data.jobs.*
import io.github.vladimirmi.photon.data.managers.Cache
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.dto.PhotocardDto
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.req.EditAlbumReq
import io.github.vladimirmi.photon.utils.ErrorObserver
import io.github.vladimirmi.photon.utils.ioToMain
import io.github.vladimirmi.photon.utils.justOrEmpty
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
        Observable.just(dataManager.getDetachedObjFromDb(Album::class.java, id)?.updated ?: Date(0))
                .flatMap { dataManager.getAlbumFromNet(id, getUpdated(it)) }
                .doOnNext { dataManager.saveToDB(it) }
                .subscribeOn(Schedulers.io())
                .subscribeWith(ErrorObserver())
    }

    override fun getProfileId() = dataManager.getProfileId()

    override fun editAlbum(albumReq: EditAlbumReq): Single<Unit> {
        val job = EditAlbumJob(albumReq)
        return jobManager.singleCancelJobs(TagConstraint.ANY, job.tag)
                .map { jobManager.addJobInBackground(job) }
                .ioToMain()
    }

    override fun deleteAlbum(albumId: String): Single<Unit> {

        return Single.just(dataManager.getDetachedObjFromDb(Album::class.java, albumId))
                .flatMap { removePhotosById(it.photocards.map { it.id }) }
                .flatMap { jobManager.singleCancelJobs(TagConstraint.ANY, CreateAlbumJob.TAG + albumId) }
                .map {
                    if (it.cancelledJobs.isEmpty()) {
                        DeleteAlbumJob(albumId, skipNetworkPart = false)
                    } else {
                        DeleteAlbumJob(albumId, skipNetworkPart = true)
                    }
                }
                .map { jobManager.addJobInBackground(it) }
                .ioToMain()
    }

    override fun removePhotos(photosForDelete: List<PhotocardDto>, album: AlbumDto): Single<Unit> {
        return removePhotosById(photosForDelete.map { it.id }, album.id, album.isFavorite)
                .ioToMain()
    }

    private fun removePhotosById(photosForDelete: List<String>,
                                 albumId: String = "",
                                 isFavorite: Boolean = false): Single<Unit> {
        if (photosForDelete.isEmpty()) return Single.just(Unit)

        return Observable.fromIterable(photosForDelete)
                .flatMapSingle {
                    if (isFavorite) {
                        removeFromFavorite(it, albumId)
                    } else {
                        cancelCreateOrRemovePhoto(it)
                    }
                }
                .lastOrError()
    }

    private fun cancelCreateOrRemovePhoto(photocardId: String): Single<Unit> {
        val createPhotoJobTag = CreatePhotoJob.TAG + photocardId

        return jobManager.singleCancelJobs(TagConstraint.ANY, createPhotoJobTag)
                .map {
                    if (it.cancelledJobs.isEmpty()) {
                        DeletePhotocardJob(photocardId, skipNetworkPart = false)
                    } else {
                        DeletePhotocardJob(photocardId, skipNetworkPart = true)
                    }
                }
                .map { jobManager.addJobInBackground(it) }
    }

    private fun removeFromFavorite(id: String, favAlbumId: String): Single<Unit> {
        return jobManager.singleCancelJobs(TagConstraint.ANY, AddToFavoriteJob.TAG + id)
                .map { cancelResult ->
                    val job = if (cancelResult.cancelledJobs.isEmpty()) {
                        DeleteFromFavoriteJob(id, favAlbumId, skipNetworkPart = false)
                    } else {
                        DeleteFromFavoriteJob(id, favAlbumId, skipNetworkPart = true)
                    }
                    jobManager.addJobInBackground(job)
                    job
                }
                .flatMap { jobManager.singleResultFor(it) }
    }
}
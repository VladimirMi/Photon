package io.github.vladimirmi.photon.features.album

import io.github.vladimirmi.photon.data.jobs.queue.Jobs
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.dto.PhotocardDto
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.utils.JobStatus
import io.github.vladimirmi.photon.utils.ioToMain
import io.reactivex.Completable
import io.reactivex.Observable

/**
 * Created by Vladimir Mikhalev 19.06.2017.
 */

class AlbumModel(private val dataManager: DataManager,
                 private val jobs: Jobs) : IAlbumModel {

    override fun getAlbum(id: String): Observable<AlbumDto> {
        return dataManager.getCached<Album, AlbumDto>(id)
                .ioToMain()
    }

    override fun updateAlbum(id: String): Completable {
        return dataManager.isNetworkAvailable()
                .filter { it }
                .flatMap { dataManager.getAlbumFromNet(id) }
                .doOnNext { dataManager.saveToDB(it) }
                .ignoreElements()
                .ioToMain()
    }

    override fun getProfileId() = dataManager.getProfileId()

    override fun editAlbum(request: AlbumDto): Observable<JobStatus> =
            jobs.albumEdit(request).ioToMain()

    override fun deleteAlbum(id: String): Observable<JobStatus> =
            jobs.albumDelete(id).ioToMain()

    override fun removePhotos(photosForDelete: List<PhotocardDto>, album: AlbumDto): Observable<JobStatus> =
            removePhotosById(photosForDelete.map { it.id }, album.isFavorite).ioToMain()


    private fun removePhotosById(photosForDelete: List<String>,
                                 isFavorite: Boolean): Observable<JobStatus> {
        return if (photosForDelete.isEmpty()) {
            Observable.empty()
        } else {
            Observable.fromIterable(photosForDelete)
                    .flatMap {
                        if (isFavorite) {
                            jobs.albumDeleteFavorite(it)
                        } else {
                            jobs.albumDelete(it)
                        }
                    }
        }
    }
}
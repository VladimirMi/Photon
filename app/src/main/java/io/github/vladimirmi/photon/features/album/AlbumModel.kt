package io.github.vladimirmi.photon.features.album

import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.EditAlbumReq
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.utils.ioToMain
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

/**
 * Created by Vladimir Mikhalev 19.06.2017.
 */

class AlbumModel(private val dataManager: DataManager) : IAlbumModel {

    override fun getAlbum(id: String): Observable<Album> {
        return dataManager.getObjectFromDb(Album::class.java, id)
    }

    override fun getProfileId() = dataManager.getProfileId()

    override fun editAlbum(album: Album): Observable<Album> {
        return dataManager.editAlbum(EditAlbumReq(album.id, album.title, album.description))
                .doOnNext { dataManager.saveToDB(it) }
                .subscribeOn(Schedulers.io())
    }

    override fun deleteAlbum(album: Album): Observable<Int> {
        return dataManager.deleteAlbum(album.id)
                .doOnNext { dataManager.removeFromDb(Album::class.java, album.id) }
                .ioToMain()
    }

    override fun removePhotos(photosForDelete: List<Photocard>): Observable<Int> {
        return Observable.fromIterable(photosForDelete)
                .flatMap { photocard ->
                    dataManager.deletePhotocard(photocard.id)
                            .doOnNext { dataManager.removeFromDb(Photocard::class.java, photocard.id) }
                }
                .subscribeOn(Schedulers.io())
    }
}
package io.github.vladimirmi.photon.features.album

import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.EditAlbumReq
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.utils.ioToMain
import io.reactivex.Observable

/**
 * Created by Vladimir Mikhalev 19.06.2017.
 */

class AlbumModel(private val dataManager: DataManager) : IAlbumModel {

    override fun getAlbum(id: String): Observable<Album> {
        return dataManager.getObjectFromDb(Album::class.java, id)
    }

    override fun getProfileId() = dataManager.getProfileId()

    override fun editAlbum(albumReq: EditAlbumReq): Observable<Album> {
        return dataManager.editAlbum(albumReq)
                .doOnNext { dataManager.saveToDB(it) }
    }

    override fun deleteAlbum(album: Album): Observable<Int> {
        return dataManager.deleteAlbum(album.id)
                .doOnNext { dataManager.removeFromDb(Album::class.java, album.id) }
                .flatMap { removePhotos(album.photocards, album) }
                .ioToMain()
    }

    override fun removePhotos(photosForDelete: List<Photocard>, album: Album): Observable<Int> {
        return Observable.fromIterable(photosForDelete)
                .flatMap { photocard ->
                    if (album.isFavorite) {
                        dataManager.removeFromFavorite(photocard.id).ioToMain()
                                .doOnComplete {
                                    album.photocards.remove(photocard)
                                    dataManager.saveToDB(album)
                                }
                    } else {
                        dataManager.deletePhotocard(photocard.id).ioToMain()
                                .doOnComplete { dataManager.removeFromDb(Photocard::class.java, photocard.id) }
                    }
                }
    }
}
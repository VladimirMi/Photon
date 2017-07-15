package io.github.vladimirmi.photon.features.album

import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.EditAlbumReq
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.dto.PhotocardDto
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.utils.ioToMain
import io.github.vladimirmi.photon.utils.unit
import io.reactivex.Observable

/**
 * Created by Vladimir Mikhalev 19.06.2017.
 */

class AlbumModel(private val dataManager: DataManager) : IAlbumModel {

    override fun getAlbum(id: String): Observable<AlbumDto> {
        return dataManager.getObjectFromDb(Album::class.java, id)
                .map { AlbumDto(it) }
    }

    override fun getProfileId() = dataManager.getProfileId()

    override fun editAlbum(albumReq: EditAlbumReq): Observable<Unit> {
        return dataManager.editAlbum(albumReq)
                .map { dataManager.saveToDB(it) }
    }

    override fun deleteAlbum(albumId: String): Observable<Unit> {
        return dataManager.deleteAlbum(albumId)
                .map { dataManager.getDetachedObjFromDb(Album::class.java, albumId)!! }
                .map {
                    it.photocards.forEach { dataManager.removeFromDb(Photocard::class.java, it.id) }
                    dataManager.removeFromDb(Album::class.java, it.id)
                }
                .ioToMain()
    }

    override fun removePhotos(photosForDelete: List<PhotocardDto>, album: AlbumDto): Observable<Unit> {
        if (album.isFavorite) {
            return removeFromFavorite(photosForDelete, album)
        }
        return Observable.fromIterable(photosForDelete)
                .flatMap { photocard ->
                    dataManager.deletePhotocard(photocard.id).ioToMain()
                            .doOnComplete { dataManager.removeFromDb(Photocard::class.java, photocard.id) }
                }.unit()
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
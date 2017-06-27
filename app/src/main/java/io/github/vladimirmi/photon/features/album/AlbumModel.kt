package io.github.vladimirmi.photon.features.album

import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.EditAlbumReq
import io.github.vladimirmi.photon.data.models.realm.Album
import io.reactivex.Observable

/**
 * Created by Vladimir Mikhalev 19.06.2017.
 */

class AlbumModel(private val dataManager: DataManager) : IAlbumModel {

    override fun getAlbum(id: String): Observable<Album> {
        return dataManager.getObjectFromDb(Album::class.java, id)
    }

    override fun getProfileId() = dataManager.getProfileId()

    override fun editAlbum(album: Album) {
        dataManager.editAlbum(EditAlbumReq(album.id, album.title, album.description))
                .doOnNext { dataManager.saveToDB(it) }
                .subscribe()
    }

    override fun deleteAlbum(album: Album) {
        //todo перенести подписку в презентер, по завершению back
        dataManager.deleteAlbum(album.id)
                .doOnNext { dataManager.removeFromDb(Album::class.java, album.id) }
                .subscribe()
    }
}
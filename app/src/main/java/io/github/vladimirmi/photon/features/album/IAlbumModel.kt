package io.github.vladimirmi.photon.features.album

import io.github.vladimirmi.photon.core.IModel
import io.github.vladimirmi.photon.data.models.realm.Album
import io.reactivex.Observable

interface IAlbumModel : IModel {
    fun getAlbum(id: String): Observable<Album>
    fun getProfileId(): String
    fun editAlbum(album: Album)
}
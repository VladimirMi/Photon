package io.github.vladimirmi.photon.features.album

import io.github.vladimirmi.photon.core.IModel
import io.github.vladimirmi.photon.data.models.EditAlbumReq
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.reactivex.Observable

interface IAlbumModel : IModel {
    fun getAlbum(id: String): Observable<Album>
    fun getProfileId(): String
    fun editAlbum(albumReq: EditAlbumReq): Observable<Album>
    fun deleteAlbum(album: Album): Observable<Int>
    fun removePhotos(photosForDelete: List<Photocard>, album: Album): Observable<Int>
}
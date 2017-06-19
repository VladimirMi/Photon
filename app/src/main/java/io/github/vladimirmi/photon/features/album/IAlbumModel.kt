package io.github.vladimirmi.photon.features.album

import io.github.vladimirmi.photon.core.IModel
import io.github.vladimirmi.photon.data.models.Album
import io.reactivex.Observable

interface IAlbumModel : IModel {
    fun getAlbum(id: String): Observable<Album>
}
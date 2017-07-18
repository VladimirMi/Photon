package io.github.vladimirmi.photon.features.album

import io.github.vladimirmi.photon.core.IModel
import io.github.vladimirmi.photon.data.models.EditAlbumReq
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.dto.PhotocardDto
import io.reactivex.Observable
import io.reactivex.Single

interface IAlbumModel : IModel {
    fun getAlbum(id: String): Observable<AlbumDto>
    fun getProfileId(): String
    fun editAlbum(albumReq: EditAlbumReq): Observable<Unit>
    fun deleteAlbum(albumId: String): Single<Unit>
    fun removePhotos(photosForDelete: List<PhotocardDto>, album: AlbumDto): Observable<Unit>
}
package io.github.vladimirmi.photon.features.album

import io.github.vladimirmi.photon.core.IModel
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.dto.PhotocardDto
import io.github.vladimirmi.photon.data.models.req.AlbumEditReq
import io.github.vladimirmi.photon.utils.JobStatus
import io.reactivex.Observable
import io.reactivex.Single

interface IAlbumModel : IModel {
    fun getAlbum(id: String): Observable<AlbumDto>
    fun getProfileId(): String
    fun editAlbum(albumReq: AlbumEditReq): Observable<JobStatus>
    fun deleteAlbum(albumId: String): Observable<JobStatus>
    fun removePhotos(photosForDelete: List<PhotocardDto>, album: AlbumDto): Single<Unit>
}
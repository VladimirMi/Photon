package io.github.vladimirmi.photon.features.album

import io.github.vladimirmi.photon.core.IModel
import io.github.vladimirmi.photon.data.managers.extensions.JobStatus
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.dto.PhotocardDto
import io.github.vladimirmi.photon.data.models.req.AlbumEditReq
import io.reactivex.Observable

interface IAlbumModel : IModel {
    fun getAlbum(id: String): Observable<AlbumDto>
    fun editAlbum(request: AlbumEditReq): Observable<JobStatus>
    fun deleteAlbum(id: String): Observable<JobStatus>
    fun removePhotos(photosForDelete: List<PhotocardDto>, album: AlbumDto): Observable<JobStatus>
    fun isOwner(owner: String): Boolean
}
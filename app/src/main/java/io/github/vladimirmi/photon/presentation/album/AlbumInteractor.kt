package io.github.vladimirmi.photon.presentation.album

import io.github.vladimirmi.photon.core.Interactor
import io.github.vladimirmi.photon.data.managers.extensions.JobStatus
import io.github.vladimirmi.photon.data.models.req.AlbumEditReq
import io.github.vladimirmi.photon.domain.models.AlbumDto
import io.github.vladimirmi.photon.domain.models.PhotocardDto
import io.reactivex.Observable

interface AlbumInteractor : Interactor {
    fun getAlbum(id: String): Observable<AlbumDto>
    fun editAlbum(request: AlbumEditReq): Observable<JobStatus>
    fun deleteAlbum(id: String): Observable<JobStatus>
    fun removePhotos(photosForDelete: List<PhotocardDto>, album: AlbumDto): Observable<JobStatus>
    fun isOwner(owner: String): Boolean
}
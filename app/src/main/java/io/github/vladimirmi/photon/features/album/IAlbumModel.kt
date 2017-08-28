package io.github.vladimirmi.photon.features.album

import io.github.vladimirmi.photon.core.IModel
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.dto.PhotocardDto
import io.github.vladimirmi.photon.utils.JobStatus
import io.reactivex.Completable
import io.reactivex.Observable

interface IAlbumModel : IModel {
    fun getAlbum(id: String): Observable<AlbumDto>
    fun getProfileId(): String
    fun editAlbum(request: AlbumDto): Observable<JobStatus>
    fun deleteAlbum(id: String): Observable<JobStatus>
    fun removePhotos(photosForDelete: List<PhotocardDto>, album: AlbumDto): Observable<JobStatus>
    fun updateAlbum(id: String): Completable
}
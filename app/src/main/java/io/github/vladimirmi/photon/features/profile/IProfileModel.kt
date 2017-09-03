package io.github.vladimirmi.photon.features.profile

import io.github.vladimirmi.photon.core.IModel
import io.github.vladimirmi.photon.data.managers.extensions.JobStatus
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.dto.UserDto
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.req.ProfileEditReq
import io.reactivex.Completable
import io.reactivex.Observable

interface IProfileModel : IModel {
    fun getProfile(): Observable<UserDto>
    fun getAlbums(): Observable<List<AlbumDto>>
    fun isUserAuth(): Boolean
    fun createAlbum(album: Album): Observable<JobStatus>
    fun editProfile(request: ProfileEditReq): Observable<JobStatus>
    fun updateProfile(): Completable
}
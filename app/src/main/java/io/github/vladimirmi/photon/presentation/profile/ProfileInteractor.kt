package io.github.vladimirmi.photon.presentation.profile

import io.github.vladimirmi.photon.core.Interactor
import io.github.vladimirmi.photon.data.managers.extensions.JobStatus
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.req.ProfileEditReq
import io.github.vladimirmi.photon.domain.models.AlbumDto
import io.github.vladimirmi.photon.domain.models.UserDto
import io.reactivex.Observable

interface ProfileInteractor : Interactor {
    fun getProfile(): Observable<UserDto>
    fun getAlbums(): Observable<List<AlbumDto>>
    fun isUserAuth(): Boolean
    fun createAlbum(album: Album): Observable<JobStatus>
    fun editProfile(request: ProfileEditReq): Observable<JobStatus>
}
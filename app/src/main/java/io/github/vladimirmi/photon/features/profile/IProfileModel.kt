package io.github.vladimirmi.photon.features.profile

import io.github.vladimirmi.photon.core.IModel
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.dto.UserDto
import io.github.vladimirmi.photon.data.models.req.EditProfileReq
import io.github.vladimirmi.photon.data.models.req.NewAlbumReq
import io.github.vladimirmi.photon.utils.JobStatus
import io.reactivex.Observable

interface IProfileModel : IModel {
    fun getProfile(): Observable<UserDto>
    fun getAlbums(): Observable<List<AlbumDto>>
    fun isUserAuth(): Boolean
    fun createAlbum(newAlbumReq: NewAlbumReq): Observable<JobStatus>
    fun editProfile(profileReq: EditProfileReq): Observable<JobStatus>
}
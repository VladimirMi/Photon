package io.github.vladimirmi.photon.features.profile

import io.github.vladimirmi.photon.core.IModel
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.dto.UserDto
import io.github.vladimirmi.photon.data.models.req.EditProfileReq
import io.github.vladimirmi.photon.data.models.req.NewAlbumReq
import io.reactivex.Observable
import io.reactivex.Single

interface IProfileModel : IModel {
    fun getProfile(): Observable<UserDto>
    fun getAlbums(): Observable<List<AlbumDto>>
    fun isUserAuth(): Boolean
    fun createAlbum(newAlbumReq: NewAlbumReq): Single<Unit>
    fun editProfile(profileReq: EditProfileReq, loadAvatar: Boolean): Single<Unit>
}
package io.github.vladimirmi.photon.features.profile

import io.github.vladimirmi.photon.core.IModel
import io.github.vladimirmi.photon.data.models.EditProfileReq
import io.github.vladimirmi.photon.data.models.NewAlbumReq
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.data.network.ApiError
import io.reactivex.Observable

interface IProfileModel : IModel {
    fun getProfile(): Observable<User>
    fun getAlbums(): Observable<List<Album>>
    fun isUserAuth(): Boolean
    fun createAlbum(newAlbumReq: NewAlbumReq, profile: User): Observable<Unit>
    fun editProfile(profileReq: EditProfileReq, avatarChange: Boolean, errCallback: (ApiError?) -> Unit)
}
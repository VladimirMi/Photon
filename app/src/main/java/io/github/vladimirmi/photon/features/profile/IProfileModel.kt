package io.github.vladimirmi.photon.features.profile

import io.github.vladimirmi.photon.core.IModel
import io.github.vladimirmi.photon.data.models.Album
import io.github.vladimirmi.photon.data.models.NewAlbumReq
import io.github.vladimirmi.photon.data.models.User
import io.reactivex.Observable

interface IProfileModel : IModel {
    fun getProfile(): Observable<User>
    fun getUser(userId: String): Observable<User>
    fun isUserAuth(): Boolean
    fun createAlbum(newAlbumReq: NewAlbumReq): Observable<Album>
}
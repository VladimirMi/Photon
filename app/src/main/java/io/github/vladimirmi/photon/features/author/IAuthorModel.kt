package io.github.vladimirmi.photon.features.author

import io.github.vladimirmi.photon.core.IModel
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.User
import io.reactivex.Observable

interface IAuthorModel : IModel {
    fun getUser(userId: String): Observable<User>
    fun getAlbums(ownerId: String): Observable<List<Album>>
}
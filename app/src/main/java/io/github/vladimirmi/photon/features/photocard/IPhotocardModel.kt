package io.github.vladimirmi.photon.features.photocard

import io.github.vladimirmi.photon.core.IModel
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.models.realm.User
import io.reactivex.Observable
import io.reactivex.Single

interface IPhotocardModel : IModel {
    fun getUser(id: String): Observable<User>
    fun getPhotocard(id: String, ownerId: String): Observable<Photocard>
    fun addToFavorite(photocard: Photocard): Observable<Unit>
    fun isFavorite(photocard: Photocard): Single<Boolean>
    fun removeFromFavorite(photocard: Photocard): Observable<Unit>
}
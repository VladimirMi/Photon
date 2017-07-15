package io.github.vladimirmi.photon.features.photocard

import io.github.vladimirmi.photon.core.IModel
import io.github.vladimirmi.photon.data.models.dto.PhotocardDto
import io.github.vladimirmi.photon.data.models.dto.UserDto
import io.reactivex.Observable
import io.reactivex.Single

interface IPhotocardModel : IModel {
    fun getUser(id: String): Observable<UserDto>
    fun getPhotocard(id: String, ownerId: String): Observable<PhotocardDto>
    fun addToFavorite(id: String): Observable<Unit>
    fun isFavorite(id: String): Single<Boolean>
    fun removeFromFavorite(id: String): Observable<Unit>
}
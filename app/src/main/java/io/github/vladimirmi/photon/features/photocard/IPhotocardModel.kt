package io.github.vladimirmi.photon.features.photocard

import io.github.vladimirmi.photon.core.IModel
import io.github.vladimirmi.photon.data.models.Photocard
import io.github.vladimirmi.photon.data.models.User
import io.reactivex.Observable

interface IPhotocardModel : IModel {
    fun getUser(id: String): Observable<User>
    fun getPhotocard(id: String, ownerId: String): Observable<Photocard>
}
package io.github.vladimirmi.photon.features.profile

import io.github.vladimirmi.photon.core.IModel
import io.github.vladimirmi.photon.data.models.User
import io.reactivex.Observable

interface IProfileModel : IModel {
    fun getUser(): Observable<User>
    fun isUserAuth(): Boolean
}
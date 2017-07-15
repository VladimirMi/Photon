package io.github.vladimirmi.photon.features.auth

import io.github.vladimirmi.photon.core.IModel
import io.reactivex.Observable

/**
 * Created by Vladimir Mikhalev 25.06.2017.
 */

interface IAuthModel : IModel {
    fun isNetAvail(): Observable<Boolean>
}
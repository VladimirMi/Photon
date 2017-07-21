package io.github.vladimirmi.photon.features.splash

import io.github.vladimirmi.photon.core.IModel
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

interface ISplashModel : IModel {
    fun updateAll(limit: Int): Observable<Unit>
    fun dbIsNotEmpty(): Single<Boolean>
}

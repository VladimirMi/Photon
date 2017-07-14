package io.github.vladimirmi.photon.features.splash

import io.github.vladimirmi.photon.core.IModel
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.reactivex.Observable

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

interface ISplashModel : IModel {
    fun updateLimitPhotoCards(limit: Int, minDelay: Long): Observable<List<Photocard>>
    fun dbIsNotEmpty(): Boolean
}

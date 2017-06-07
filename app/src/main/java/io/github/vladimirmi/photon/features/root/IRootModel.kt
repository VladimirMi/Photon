package io.github.vladimirmi.photon.features.root

import io.github.vladimirmi.photon.core.IModel
import io.reactivex.Observable

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

interface IRootModel : IModel {
    fun updatePhotoCards(): Observable<Int>
}

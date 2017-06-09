package io.github.vladimirmi.photon.features.main

import io.github.vladimirmi.photon.core.IModel
import io.github.vladimirmi.photon.data.models.Photocard
import io.reactivex.Observable

/**
 * Developer Vladimir Mikhalev, 03.06.2017.
 */

interface IMainModel : IModel {
    fun getPhotoCards(): Observable<List<Photocard>>
}
package io.github.vladimirmi.photon.features.main

import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.network.models.Photocard
import io.reactivex.Observable

/**
 * Developer Vladimir Mikhalev, 03.06.2017.
 */

class MainModel(private val dataManager: DataManager) : IMainModel {


    override fun getPhotoCards(): Observable<List<Photocard>> {
        return dataManager.getFromDb(Photocard::class.java)
    }
}
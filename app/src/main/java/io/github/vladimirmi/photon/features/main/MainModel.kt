package io.github.vladimirmi.photon.features.main

import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.Photocard
import io.github.vladimirmi.photon.features.search.SearchView
import io.reactivex.Observable

/**
 * Developer Vladimir Mikhalev, 03.06.2017.
 */

class MainModel(private val dataManager: DataManager) : IMainModel {

    override val searchQuery = HashMap<String, MutableList<String>>()

    override fun makeQuery(searchQuery: HashMap<String, MutableList<String>>, currentPage: SearchView.Page) {
        TODO("not implemented")
    }

    override fun getPhotoCards(): Observable<List<Photocard>> {
        return dataManager.getFromDb(Photocard::class.java)
    }
}
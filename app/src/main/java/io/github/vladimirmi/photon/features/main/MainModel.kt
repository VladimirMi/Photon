package io.github.vladimirmi.photon.features.main

import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.features.search.SearchView
import io.github.vladimirmi.photon.utils.Query
import io.github.vladimirmi.photon.utils.ioToMain
import io.reactivex.Observable
import io.realm.Sort

/**
 * Developer Vladimir Mikhalev, 03.06.2017.
 */

class MainModel(val dataManager: DataManager) : IMainModel {

    var query = ArrayList<Query>()
    override var appliedPage: SearchView.Page = SearchView.Page.TAGS
    override val tagsQuery = ArrayList<Query>()
    override val filtersQuery = ArrayList<Query>()


    override fun makeQuery(currentPage: SearchView.Page) {
        when (currentPage) {
            SearchView.Page.TAGS -> query = tagsQuery
            SearchView.Page.FILTERS -> query = filtersQuery
        }
        appliedPage = currentPage
    }

    override fun getPhotoCards(): Observable<List<Photocard>> {
        return dataManager.search(Photocard::class.java,
                query = if (query.isNotEmpty()) query.toList() else null,
                sortBy = "updated",
                order = Sort.DESCENDING)
    }

    override fun isFiltered() = query.isNotEmpty()

    override fun resetFilter() {
        query.clear()
        tagsQuery.clear()
        filtersQuery.clear()
        appliedPage = SearchView.Page.TAGS
    }

    override fun addView(photocard: Photocard): Observable<Unit> {
        return dataManager.addView(photocard.id)
                .map {
                    if (it.success) {
                        photocard.views++
                        dataManager.saveToDB(photocard)
                    }
                }
                .ioToMain()
    }

    override fun updatePhotocards(offset: Int, limit: Int): Observable<List<Photocard>> {
        return dataManager.getPhotocardsFromNet(offset, limit)
                .doOnNext { it.forEach { dataManager.saveToDB(it) } }
                .ioToMain()
    }
}
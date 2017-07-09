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

    override val query = ArrayList<Query>()
    override var appliedPage: SearchView.Page = SearchView.Page.TAGS
    private val searchQuery = ArrayList<Query>()

    private fun pageFilter(query: Query, page: SearchView.Page): Boolean {
        return when (page) {
            SearchView.Page.TAGS -> query.fieldName == "search" || query.fieldName == "tags.value"
            SearchView.Page.FILTERS -> query.fieldName != "search" && query.fieldName != "tags.value"
        }
    }

    override fun makeQuery(queryList: List<Query>, currentPage: SearchView.Page) {
        searchQuery.clear()
        searchQuery.addAll(queryList.filter { pageFilter(it, currentPage) })
        appliedPage = currentPage
    }

    override fun getPhotoCards(): Observable<List<Photocard>> {
        return dataManager.search(Photocard::class.java,
                query = if (searchQuery.isNotEmpty()) searchQuery.toList() else null,
                sortBy = "views",
                order = Sort.DESCENDING)
    }

    override fun isFiltered() = searchQuery.isNotEmpty()

    override fun resetFilter() {
        searchQuery.clear()
        query.clear()
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
}
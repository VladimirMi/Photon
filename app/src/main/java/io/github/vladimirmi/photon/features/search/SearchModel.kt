package io.github.vladimirmi.photon.features.search

import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.managers.Query
import io.github.vladimirmi.photon.data.managers.RealmOperator
import io.github.vladimirmi.photon.data.models.Search
import io.github.vladimirmi.photon.data.models.Tag
import io.github.vladimirmi.photon.features.main.IMainModel
import io.reactivex.Observable
import io.realm.Sort
import timber.log.Timber

/**
 * Developer Vladimir Mikhalev, 06.06.2017.
 */

class SearchModel(private val dataManager: DataManager, private val mainModel: IMainModel) : ISearchModel {

    override var page: SearchView.Page = SearchView.Page.TAGS

    override fun getTags(): Observable<List<Tag>> {
        return dataManager.getListFromDb(Tag::class.java, "tag")
    }

    override fun getQuery(): HashMap<String, MutableList<String>> {
        return mainModel.searchQuery
    }

    override fun addQuery(query: Pair<String, String>) {
        if (mainModel.searchQuery[query.first] == null) {
            mainModel.searchQuery[query.first] = mutableListOf()
        }
        if (query.first == "search") {
            mainModel.searchQuery[query.first] = mutableListOf(query.second)
        } else {
            mainModel.searchQuery[query.first]!!.add(query.second)
        }
        Timber.e("addQuery $query to ${mainModel.searchQuery}")
    }

    override fun makeQuery() {
        mainModel.makeQuery(mainModel.searchQuery, page)
    }

    override fun search(string: String): Observable<List<Search>> {
        val query = Query("value", RealmOperator.CONTAINS, string)
        return dataManager.search(Search::class.java, listOf(query),
                sortBy = "date", order = Sort.DESCENDING)
                .map { if (it.size > 5) it.subList(0, 5) else it }
    }

    override fun saveSearch(search: String) {
        dataManager.saveToDB(Search(search), async = true)
    }
}
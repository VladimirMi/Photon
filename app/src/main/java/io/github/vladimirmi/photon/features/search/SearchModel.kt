package io.github.vladimirmi.photon.features.search

import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.Tag
import io.github.vladimirmi.photon.features.main.IMainModel
import io.reactivex.Observable

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
    }

    override fun makeQuery() {
        mainModel.makeQuery(mainModel.searchQuery, page)
    }
}
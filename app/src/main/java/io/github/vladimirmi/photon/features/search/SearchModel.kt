package io.github.vladimirmi.photon.features.search

import io.github.vladimirmi.photon.data.managers.Cache
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.realm.Search
import io.github.vladimirmi.photon.data.models.realm.Tag
import io.github.vladimirmi.photon.features.main.IMainModel
import io.github.vladimirmi.photon.utils.Query
import io.github.vladimirmi.photon.utils.RealmOperator
import io.github.vladimirmi.photon.utils.ioToMain
import io.reactivex.Observable
import io.realm.Sort
import timber.log.Timber

/**
 * Developer Vladimir Mikhalev, 06.06.2017.
 */

class SearchModel(val dataManager: DataManager, val mainModel: IMainModel, val cache: Cache) : ISearchModel {

    override var queryPage = mainModel.queryPage

    override fun getTags(): Observable<List<String>> {
        val tags = dataManager.getListFromDb(Tag::class.java, "value")
                .map { cache.cacheTags(it) }
                .map { cache.tags }

        return Observable.merge(Observable.just(cache.tags), tags).ioToMain()
    }

    override fun getQuery(): MutableList<Query> {
        return when (queryPage) {
            SearchView.Page.TAGS -> mainModel.tagsQuery
            SearchView.Page.FILTERS -> mainModel.filtersQuery
        }
    }

    override fun addQuery(pair: Pair<String, String>) {
        Timber.e("addQuery $pair to ${getQuery()}")
        getQuery().add(parseToQuery(pair))
    }

    override fun removeQuery(pair: Pair<String, String>) {
        Timber.e("removeQuery $pair from ${getQuery()}")
        getQuery().removeAll { it.fieldName == pair.first && it.value == pair.second }
    }

    override fun removeQuery(fieldName: String) {
        Timber.e("removeQuery $fieldName from ${getQuery()}")
        getQuery().removeAll { it.fieldName == fieldName }
    }

    private fun parseToQuery(pair: Pair<String, String>): Query {
        val operator: RealmOperator
        if (pair.first == "filters.nuances" || pair.first == "search") {
            operator = RealmOperator.CONTAINS
        } else {
            operator = RealmOperator.EQUALTO
        }
        return Query(pair.first, operator, pair.second)
    }

    override fun makeQuery() {
        mainModel.makeQuery(queryPage)
    }

    override fun search(string: String): Observable<List<String>> {
        val query = Query("value", RealmOperator.CONTAINS, string)
        val searches = dataManager.search(Search::class.java, listOf(query),
                sortBy = "date", order = Sort.DESCENDING)
                .map { cache.cacheSearches(it) }
                .map { cache.searches }

        return Observable.merge(Observable.just(cache.searches), searches)
                .map { if (it.size > 5) it.subList(0, 5) else it }
                .ioToMain()
    }

    override fun saveSearchField(search: String) {
        dataManager.saveToDB(Search(search))
    }
}
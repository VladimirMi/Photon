package io.github.vladimirmi.photon.features.search

import io.github.vladimirmi.photon.data.models.realm.Search
import io.github.vladimirmi.photon.data.repository.photocard.PhotocardRepository
import io.github.vladimirmi.photon.features.main.IMainModel
import io.github.vladimirmi.photon.utils.Query
import io.reactivex.Observable
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Developer Vladimir Mikhalev, 06.06.2017.
 */

//todo search repository
class SearchModel(private val mainModel: IMainModel,
                  private val photocardRepository: PhotocardRepository)
    : ISearchModel {

    override var queryPage
        get() = mainModel.queryPage
        set(value) {
            mainModel.queryPage = value
        }
    override var tagsQuery
        get() = mainModel.tagsQuery
        set(value) {
            mainModel.tagsQuery = value
        }
    override var filtersQuery
        get() = mainModel.filtersQuery
        set(value) {
            mainModel.filtersQuery = value
        }

    override fun getTags(): Observable<List<String>> {
        val pageSize = 20
        return photocardRepository.getTags()
                .map { it.map { it.value } }
                .flatMap { list ->
                    Observable.interval(0, 500, TimeUnit.MILLISECONDS)
                            .flatMap { long ->
                                val from = (pageSize * long).toInt()
                                val to = (pageSize * (long + 1)).toInt()
                                when {
                                    from > list.size -> Observable.empty()
                                    to > list.size -> Observable.just(list.subList(from, list.size))
                                    else -> Observable.just(list.subList(from, to))
                                }
                            }
                }
    }

    override fun isFiltered() = mainModel.isFiltered()

    override fun getQuery(): ArrayList<Query> = when (queryPage) {
        SearchView.Page.TAGS -> tagsQuery
        SearchView.Page.FILTERS -> filtersQuery
    }

    override fun addQuery(pair: Pair<String, String>) {
        Timber.e("addQuery: $pair")
        getQuery().add(parseToQuery(pair))
    }

    override fun addQuery(query: Query) {
        getQuery().add(query)
    }

    override fun removeQuery(pair: Pair<String, String>) {
        getQuery().removeAll { it.fieldName == pair.first && it.value == pair.second }
    }

    override fun removeQuery(fieldName: String) {
        getQuery().removeAll { it.fieldName == fieldName }
    }

    private fun parseToQuery(pair: Pair<String, String>): Query {
        val operator = if (pair.first == "filters.nuances" || pair.first == "searchName") {
            Query.Operator.CONTAINS
        } else {
            Query.Operator.EQUAL
        }
        return Query(pair.first, operator, pair.second)
    }

    override fun makeQuery() {
        Timber.e("makeQuery: ")
        mainModel.makeQuery()
    }

    override fun searchRecents(string: String): Observable<List<String>> {
//        val query = Query("value", Query.Operator.CONTAINS, string)
//        return dataManager.search(Search::class.java, listOf(query),
//                sortBy = "date", order = Sort.DESCENDING)
//                .map { it.map { it.value } }
//                .map { if (it.size > 5) it.subList(0, 5).map { it } else it }
//                .ioToMain()
        return Observable.empty()
    }

    override fun saveSearchField(search: String) {
        photocardRepository.save(Search(search))
    }
}
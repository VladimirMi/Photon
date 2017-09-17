package io.github.vladimirmi.photon.domain.interactors

import io.github.vladimirmi.photon.data.managers.utils.Query
import io.github.vladimirmi.photon.data.models.realm.Search
import io.github.vladimirmi.photon.data.repository.recents.RecentsRepository
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.presentation.main.MainInteractor
import io.github.vladimirmi.photon.presentation.search.SearchInteractor
import io.github.vladimirmi.photon.presentation.search.SearchScreen
import io.github.vladimirmi.photon.presentation.search.SearchView
import io.github.vladimirmi.photon.utils.ioToMain
import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Developer Vladimir Mikhalev, 06.06.2017.
 */

@DaggerScope(SearchScreen::class)
class SearchInteractorImpl
@Inject constructor(private val mainInteractor: MainInteractor,
                    private val recentsRepository: RecentsRepository)
    : SearchInteractor {

    override var queryPage
        get() = mainInteractor.queryPage
        set(value) {
            mainInteractor.queryPage = value
        }
    override var tagsQuery
        get() = mainInteractor.tagsQuery
        set(value) {
            mainInteractor.tagsQuery = value
        }
    override var filtersQuery
        get() = mainInteractor.filtersQuery
        set(value) {
            mainInteractor.filtersQuery = value
        }

    override fun getTags(): Observable<List<String>> {
        val pageSize = 20
        return recentsRepository.getTags()
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
                }.ioToMain()
    }

    override fun isFiltered() = mainInteractor.isFiltered()

    override fun getQuery(): ArrayList<Query> = when (queryPage) {
        SearchView.Page.TAGS -> tagsQuery
        SearchView.Page.FILTERS -> filtersQuery
    }

    override fun addQuery(pair: Pair<String, String>) {
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
            Query.Operator.EQUAL_TO
        }
        return Query(pair.first, operator, pair.second)
    }

    override fun makeQuery() {
        mainInteractor.makeQuery()
    }

    override fun searchRecents(string: String): Observable<List<String>> =
            recentsRepository.searchRecents(string)
                    .map { it.map { it.value } }
                    .map { if (it.size > 5) it.subList(0, 5).map { it } else it }
                    .ioToMain()

    override fun saveSearchField(search: String) {
        recentsRepository.save(Search(search))
    }
}
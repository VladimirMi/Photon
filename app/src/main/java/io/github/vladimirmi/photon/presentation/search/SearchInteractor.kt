package io.github.vladimirmi.photon.presentation.search

import io.github.vladimirmi.photon.core.Interactor
import io.github.vladimirmi.photon.data.managers.utils.Query
import io.reactivex.Observable

interface SearchInteractor : Interactor {
    var queryPage: SearchView.Page
    var tagsQuery: ArrayList<Query>
    var filtersQuery: ArrayList<Query>
    fun getTags(): Observable<List<String>>
    fun getQuery(): List<Query>
    //todo remove duplicate
    fun addQuery(pair: Pair<String, String>)

    fun addQuery(query: Query)
    fun removeQuery(pair: Pair<String, String>)
    fun removeQuery(fieldName: String)
    fun makeQuery()
    fun searchRecents(string: String): Observable<List<String>>
    fun saveSearchField(search: String)
    fun isFiltered(): Boolean
}
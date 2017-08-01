package io.github.vladimirmi.photon.features.search

import io.github.vladimirmi.photon.core.IModel
import io.github.vladimirmi.photon.utils.Query
import io.reactivex.Observable

interface ISearchModel : IModel {
    fun getTags(): Observable<List<String>>
    fun addQuery(pair: Pair<String, String>)
    fun removeQuery(pair: Pair<String, String>)
    fun makeQuery()
    fun getQuery(): List<Query>
    var queryPage: SearchView.Page
    var tagsQuery: ArrayList<Query>
    var filtersQuery: ArrayList<Query>
    fun searchRecents(string: String): Observable<List<String>>
    fun saveSearchField(search: String)
    fun removeQuery(fieldName: String)
    fun addQuery(query: Query)
    fun isFiltered(): Boolean
}
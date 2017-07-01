package io.github.vladimirmi.photon.features.search

import io.github.vladimirmi.photon.core.IModel
import io.github.vladimirmi.photon.data.managers.Query
import io.github.vladimirmi.photon.data.models.realm.Search
import io.github.vladimirmi.photon.data.models.realm.Tag
import io.reactivex.Observable

interface ISearchModel : IModel {
    fun getTags(): Observable<List<Tag>>
    fun addQuery(pair: Pair<String, String>)
    fun removeQuery(query: Pair<String, String>)
    fun makeQuery()
    fun getQuery(): List<Query>
    var page: SearchView.Page
    fun search(string: String): Observable<List<Search>>
    fun saveSearchField(search: String)
}
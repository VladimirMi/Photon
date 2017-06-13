package io.github.vladimirmi.photon.features.search

import io.github.vladimirmi.photon.core.IModel
import io.github.vladimirmi.photon.data.models.Tag
import io.reactivex.Observable

interface ISearchModel : IModel {
    fun getTags(): Observable<List<Tag>>
    fun addQuery(query: Pair<String, String>)
    fun makeQuery()
    fun getQuery(): HashMap<String, MutableList<String>>
    var page: SearchView.Page
}
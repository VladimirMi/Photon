package io.github.vladimirmi.photon.features.main

import io.github.vladimirmi.photon.core.IModel
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.features.search.SearchView
import io.github.vladimirmi.photon.utils.Query
import io.reactivex.Observable

/**
 * Developer Vladimir Mikhalev, 03.06.2017.
 */

interface IMainModel : IModel {

    fun getPhotoCards(): Observable<List<Photocard>>
    val query: MutableList<Query>
    var appliedPage: SearchView.Page
    fun makeQuery(queryList: List<Query>, currentPage: SearchView.Page)
    fun isFiltered(): Boolean
    fun resetFilter()
    fun addView(photocard: Photocard): Observable<Unit>
}
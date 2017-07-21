package io.github.vladimirmi.photon.features.main

import io.github.vladimirmi.photon.core.IModel
import io.github.vladimirmi.photon.data.models.dto.PhotocardDto
import io.github.vladimirmi.photon.features.search.SearchView
import io.github.vladimirmi.photon.utils.Query
import io.reactivex.Observable

/**
 * Developer Vladimir Mikhalev, 03.06.2017.
 */

interface IMainModel : IModel {

    fun getPhotoCards(): Observable<List<PhotocardDto>>
    val tagsQuery: MutableList<Query>
    val filtersQuery: MutableList<Query>
    var queryPage: SearchView.Page
    fun makeQuery(currentPage: SearchView.Page)
    fun isFiltered(): Boolean
    fun resetFilter()
    fun addView(photocardId: String)
    fun updatePhotocards(offset: Int, limit: Int): Observable<Unit>
}
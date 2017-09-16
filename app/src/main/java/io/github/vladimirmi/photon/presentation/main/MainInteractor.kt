package io.github.vladimirmi.photon.presentation.main

import io.github.vladimirmi.photon.core.Interactor
import io.github.vladimirmi.photon.data.managers.extensions.JobStatus
import io.github.vladimirmi.photon.domain.models.PhotocardDto
import io.github.vladimirmi.photon.presentation.search.SearchView
import io.github.vladimirmi.photon.utils.Query
import io.reactivex.Completable
import io.reactivex.Observable

/**
 * Developer Vladimir Mikhalev, 03.06.2017.
 */

interface MainInteractor : Interactor {

    fun getPhotoCards(): Observable<List<PhotocardDto>>
    var tagsQuery: ArrayList<Query>
    var filtersQuery: ArrayList<Query>
    var queryPage: SearchView.Page
    fun makeQuery()
    fun isFiltered(): Boolean
    fun resetFilter()
    fun addView(photocardId: String): Observable<JobStatus>
    fun updatePhotocards(offset: Int, limit: Int): Completable
}
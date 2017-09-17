package io.github.vladimirmi.photon.presentation.main

import io.github.vladimirmi.photon.core.Interactor
import io.github.vladimirmi.photon.data.managers.utils.JobStatus
import io.github.vladimirmi.photon.data.managers.utils.Query
import io.github.vladimirmi.photon.domain.models.PhotocardDto
import io.github.vladimirmi.photon.presentation.search.SearchView
import io.reactivex.Completable
import io.reactivex.Observable

/**
 * Developer Vladimir Mikhalev, 03.06.2017.
 */

interface MainInteractor : Interactor {
    var tagsQuery: ArrayList<Query>
    var filtersQuery: ArrayList<Query>
    var queryPage: SearchView.Page
    fun getPhotoCards(): Observable<List<PhotocardDto>>
    fun makeQuery()
    fun isFiltered(): Boolean
    fun resetFilter()
    fun addView(photocardId: String): Observable<JobStatus>
    fun updatePhotocards(offset: Int, limit: Int): Completable
}
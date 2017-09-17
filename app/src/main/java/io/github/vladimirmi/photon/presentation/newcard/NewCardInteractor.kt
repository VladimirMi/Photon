package io.github.vladimirmi.photon.presentation.newcard

import io.github.vladimirmi.photon.core.Interactor
import io.github.vladimirmi.photon.data.managers.utils.JobStatus
import io.github.vladimirmi.photon.domain.models.AlbumDto
import io.reactivex.Observable

interface NewCardInteractor : Interactor {
    var screenInfo: NewCardScreenInfo
    fun addFilter(filter: Pair<String, String>)
    fun removeFilter(filter: Pair<String, String>)
    fun searchTag(tag: String): Observable<List<String>>
    fun addTag(tag: String)
    fun getAlbums(): Observable<List<AlbumDto>>
    fun uploadPhotocard(): Observable<JobStatus>
    fun getPageError(page: Page): Int?
}
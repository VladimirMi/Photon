package io.github.vladimirmi.photon.features.newcard

import io.github.vladimirmi.photon.core.IModel
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.utils.JobStatus
import io.reactivex.Observable

interface INewCardModel : IModel {
    var screenInfo: NewCardScreenInfo
    fun addFilter(filter: Pair<String, String>)
    fun removeFilter(filter: Pair<String, String>)
    fun searchTag(tag: String): Observable<List<String>>
    fun addTag(tag: String)
    fun getAlbums(): Observable<List<AlbumDto>>
    fun uploadPhotocard(): Observable<JobStatus>
    fun getPageError(page: Page): Int?
}
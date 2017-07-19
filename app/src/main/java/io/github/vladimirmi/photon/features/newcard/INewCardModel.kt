package io.github.vladimirmi.photon.features.newcard

import io.github.vladimirmi.photon.core.IModel
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.reactivex.Observable
import io.reactivex.Single

interface INewCardModel : IModel {
    var photoCard: Photocard
    fun addFilter(filter: Pair<String, String>)
    fun removeFilter(filter: Pair<String, String>)
    fun searchTag(tag: String): Observable<List<String>>
    fun addTag(tag: String)
    fun getAlbums(): Observable<List<AlbumDto>>
    fun savePhotoUri(uri: String)
    fun uploadPhotocard(): Single<Unit>
}
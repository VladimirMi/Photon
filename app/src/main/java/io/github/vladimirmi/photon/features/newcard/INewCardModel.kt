package io.github.vladimirmi.photon.features.newcard

import io.github.vladimirmi.photon.core.IModel
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.models.realm.Tag
import io.reactivex.Observable
import io.reactivex.Single

interface INewCardModel : IModel {
    var photoCard: Photocard
    fun addFilter(filter: Pair<String, String>)
    fun removeFilter(filter: Pair<String, String>)
    fun search(tag: String): Observable<List<Tag>>
    fun addTag(tag: Tag)
    fun getAlbums(): Observable<List<Album>>
    fun savePhotoUri(uri: String)
    fun uploadPhotocard(): Single<Unit>
}
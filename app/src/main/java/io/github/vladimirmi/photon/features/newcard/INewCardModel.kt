package io.github.vladimirmi.photon.features.newcard

import io.github.vladimirmi.photon.core.IModel
import io.github.vladimirmi.photon.data.models.Album
import io.github.vladimirmi.photon.data.models.Tag
import io.reactivex.Observable

interface INewCardModel : IModel {
    fun addFilter(filter: Pair<String, String>)
    fun removeFilter(filter: Pair<String, String>)
    fun search(tag: String): Observable<List<Tag>>
    fun addTag(tag: Tag)
    fun getSavedTags(): List<Tag>
    fun getAlbums(): Observable<List<Album>>
}
package io.github.vladimirmi.photon.features.search

import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.Tag
import io.reactivex.Observable

/**
 * Developer Vladimir Mikhalev, 06.06.2017.
 */

class SearchModel(private val dataManager: DataManager) : ISearchModel {

    override fun getTags(): Observable<List<Tag>> {
        return dataManager.getFromDb(Tag::class.java)
    }
}
package io.github.vladimirmi.photon.data.repository.recents

import io.github.vladimirmi.photon.core.App
import io.github.vladimirmi.photon.data.managers.PreferencesManager
import io.github.vladimirmi.photon.data.managers.RealmManager
import io.github.vladimirmi.photon.data.models.realm.Search
import io.github.vladimirmi.photon.data.models.realm.Tag
import io.github.vladimirmi.photon.data.network.api.RestService
import io.github.vladimirmi.photon.data.network.parseGetResponse
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.utils.Query
import io.reactivex.Completable
import io.reactivex.Observable
import io.realm.RealmObject
import io.realm.Sort
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 06.09.2017.
 */

@DaggerScope(App::class)
class RecentsRepository
@Inject constructor(private val restService: RestService,
                    private val preferencesManager: PreferencesManager,
                    private val realmManager: RealmManager) {

    fun getTags(): Observable<List<Tag>> =
            realmManager.search(Tag::class.java, query = null, sortBy = "value")

    fun updateTags(): Completable {
        val tag = Tag::class.java.simpleName
        return restService.getTags(preferencesManager.getLastUpdate(tag))
                .parseGetResponse { preferencesManager.saveLastUpdate(tag, it) }
                .doOnSuccess { realmManager.save(it) }
                .ignoreElement()
    }

    fun searchTag(tag: String): Observable<List<Tag>> {
        val query = Query("value", Query.Operator.CONTAINS, tag)
        return realmManager.search(Tag::class.java, listOf(query), sortBy = "value")
    }

    fun searchRecents(string: String): Observable<List<Search>> {
        val query = Query("value", Query.Operator.CONTAINS, string)
        return realmManager.search(Search::class.java, listOf(query),
                sortBy = "date", order = Sort.DESCENDING)
    }

    fun save(it: RealmObject) = realmManager.save(it)
}
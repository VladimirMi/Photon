package io.github.vladimirmi.photon.data.repository.user

import io.github.vladimirmi.photon.core.App
import io.github.vladimirmi.photon.data.jobs.JobsManager
import io.github.vladimirmi.photon.data.managers.RealmManager
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.data.network.NetworkChecker
import io.github.vladimirmi.photon.data.network.api.RestService
import io.github.vladimirmi.photon.data.network.parseGetResponse
import io.github.vladimirmi.photon.data.repository.BaseEntityRepository
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.utils.Query
import io.reactivex.Maybe
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 04.09.2017.
 */

@DaggerScope(App::class)
class UserRepository
@Inject constructor(realmManager: RealmManager,
                    private val restService: RestService,
                    private val networkChecker: NetworkChecker,
                    private val jobsManager: JobsManager)
    : BaseEntityRepository(realmManager) {

    fun getUser(id: String, managed: Boolean = true): Observable<User> =
            realmManager.getObject(User::class.java, id, managed)

    fun updateUser(id: String): Maybe<User> {
        if (!jobsManager.syncComplete) return Maybe.empty()
        val lastModified = realmManager.getLastUpdated(User::class.java, id)
        return networkChecker.singleAvailavle()
                .flatMap { restService.getUser(id, lastModified) }
                .parseGetResponse()
                .doOnSuccess { saveFromNet(it) }
    }

    fun getAlbums(ownerId: String): Observable<List<Album>> {
        val query = listOf(Query("owner", Query.Operator.EQUAL, ownerId))
        return realmManager.search(Album::class.java, query)
    }
}
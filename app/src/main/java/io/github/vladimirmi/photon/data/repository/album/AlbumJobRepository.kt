package io.github.vladimirmi.photon.data.repository.album

import io.github.vladimirmi.photon.core.App
import io.github.vladimirmi.photon.data.managers.PreferencesManager
import io.github.vladimirmi.photon.data.managers.RealmManager
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.req.AlbumEditReq
import io.github.vladimirmi.photon.data.models.req.AlbumNewReq
import io.github.vladimirmi.photon.data.models.res.SuccessRes
import io.github.vladimirmi.photon.data.network.api.RestService
import io.github.vladimirmi.photon.data.network.body
import io.github.vladimirmi.photon.data.network.parseStatusCode
import io.github.vladimirmi.photon.data.network.statusCode
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.utils.Query
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 05.09.2017.
 */

@DaggerScope(App::class)
class AlbumJobRepository
@Inject constructor(realmManager: RealmManager,
                    preferencesManager: PreferencesManager,
                    private val restService: RestService)
    : AlbumEntityRepository(realmManager, preferencesManager) {

    override public fun getAlbum(id: String): Album = super.getAlbum(id)

    fun getAlbumFromNet(id: String): Single<Album> =
            restService.getAlbum(id, "any", "0").body()

    fun getNotSync(): Observable<List<Album>> = realmManager.search(Album::class.java,
            listOf(Query("sync", Query.Operator.EQUAL, false)), managed = false)

    fun create(request: AlbumNewReq): Single<Album> {
        return restService.createAlbum(preferencesManager.getProfileId(),
                request, preferencesManager.getUserToken())
                .parseStatusCode()
                .body()
                .doOnSuccess { removeTemp(request.id) }
    }

    fun delete(id: String): Completable {
        return restService.deleteAlbum(preferencesManager.getProfileId(),
                id, preferencesManager.getUserToken())
                .parseStatusCode()
                .toCompletable()
                .doOnComplete { realmManager.remove(Album::class.java, id) }
    }

    fun edit(req: AlbumEditReq): Single<Album> {
        return restService.editAlbum(preferencesManager.getProfileId(),
                req.id, req, preferencesManager.getUserToken())
                .parseStatusCode()
                .body()
    }


    fun addToFavorite(id: String): Single<SuccessRes> {
        return restService.addToFavorite(preferencesManager.getProfileId(),
                id, preferencesManager.getUserToken())
                .parseStatusCode()
                .body()
    }

    fun removeFromFavorite(id: String): Single<Int> {
        return restService.removeFromFavorite(preferencesManager.getProfileId(),
                id, preferencesManager.getUserToken())
                .parseStatusCode()
                .statusCode()
    }

    fun rollbackDelete(id: String) {
        with(getAlbum(id)) {
            active = true
            save(this)
        }
    }

    fun rollbackEdit(request: AlbumEditReq) {
        getAlbum(request.id).edit(request)
    }

    fun rollbackAddFavorite(id: String) {
        getAlbum(preferencesManager.getFavAlbumId()).deleteFavorite(id)
    }

    fun rollbackRemoveFavorite(id: String) {
        getAlbum(preferencesManager.getFavAlbumId()).addFavorite(id)
    }

    private fun removeTemp(id: String) {
        if (!id.startsWith("TEMP")) throw UnsupportedOperationException()
        realmManager.remove(Album::class.java, id)
    }
}
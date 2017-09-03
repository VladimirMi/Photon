package io.github.vladimirmi.photon.features.author

import io.github.vladimirmi.photon.data.managers.Cache
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.dto.UserDto
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.utils.Query
import io.github.vladimirmi.photon.utils.RealmOperator
import io.github.vladimirmi.photon.utils.ioToMain
import io.reactivex.Completable
import io.reactivex.Observable

class AuthorModel(val dataManager: DataManager, val cache: Cache) : IAuthorModel {

    override fun getUser(id: String): Observable<UserDto> {
        return dataManager.getCached<User, UserDto>(id)
                .ioToMain()
    }

    override fun updateUser(id: String): Completable {
        return dataManager.isNetworkAvailable()
                .filter { it }
                .flatMap { dataManager.getUserFromNet(id) }
                .doOnNext { dataManager.saveFromServer(it) }
                .ignoreElements()
                .ioToMain()
    }

    override fun getAlbums(ownerId: String): Observable<List<AlbumDto>> {
        val query = listOf(Query("owner", RealmOperator.EQUALTO, ownerId))
        return dataManager.search(Album::class.java, query)
                .map { cache.cacheAlbums(it) }
                .ioToMain()
    }
}
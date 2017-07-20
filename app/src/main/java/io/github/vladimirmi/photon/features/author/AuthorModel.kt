package io.github.vladimirmi.photon.features.author

import io.github.vladimirmi.photon.data.managers.Cache
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.dto.UserDto
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.utils.*
import io.reactivex.Observable

class AuthorModel(val dataManager: DataManager, val cache: Cache) : IAuthorModel {

    override fun getUser(userId: String): Observable<UserDto> {
        updateUser(userId)
        val user = dataManager.getObjectFromDb(User::class.java, userId)
                .flatMap { justOrEmpty(cache.cacheUser(it)) }
                .ioToMain()

        return Observable.merge(justOrEmpty(cache.user(userId)), user)
    }

    private fun updateUser(id: String) {
        val user = dataManager.getDetachedObjFromDb(User::class.java, id)

        dataManager.getUserFromNet(id, getUpdated(user).toString())
                .subscribeWith(object : ErrorObserver<User>() {
                    override fun onNext(it: User) {
                        dataManager.saveToDB(it)
                    }
                })
    }

    override fun getAlbums(ownerId: String): Observable<List<AlbumDto>> {
        val query = listOf(Query("owner", RealmOperator.EQUALTO, ownerId))
        return dataManager.search(Album::class.java, query, sortBy = "id")
                .map { cache.cacheAlbums(it) }
                .ioToMain()
    }
}
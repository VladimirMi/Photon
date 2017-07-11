package io.github.vladimirmi.photon.features.author

import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.utils.ErrorObserver
import io.github.vladimirmi.photon.utils.Query
import io.github.vladimirmi.photon.utils.RealmOperator
import io.reactivex.Observable

class AuthorModel(private val dataManager: DataManager) : IAuthorModel {

    override fun getUser(userId: String): Observable<User> {
        updateUser(userId)
        return dataManager.getObjectFromDb(User::class.java, userId)
    }

    private fun updateUser(id: String) {
        val user = dataManager.getSingleObjFromDb(User::class.java, id)

        dataManager.getUserFromNet(id, getUpdated(user).toString())
                .subscribeWith(object : ErrorObserver<User>() {
                    override fun onNext(it: User) {
                        dataManager.saveToDB(it)
                    }
                })
    }

    override fun getAlbums(ownerId: String): Observable<List<Album>> {
        val query = listOf(Query("owner", RealmOperator.EQUALTO, ownerId))
        return dataManager.search(Album::class.java, query, sortBy = "id")
    }
}
package io.github.vladimirmi.photon.features.photocard

import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.utils.ErrorObserver
import io.github.vladimirmi.photon.utils.Query
import io.github.vladimirmi.photon.utils.RealmOperator
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Created by Vladimir Mikhalev 14.06.2017.
 */

class PhotocardModel(private val dataManager: DataManager) : IPhotocardModel {

    override fun getUser(id: String): Observable<User> {
        updateUser(id)
        return dataManager.getObjectFromDb(User::class.java, id)
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

    override fun getPhotocard(id: String, ownerId: String): Observable<Photocard> {
        updatePhotocard(id, ownerId)
        return dataManager.getObjectFromDb(Photocard::class.java, id)
    }

    private fun updatePhotocard(id: String, ownerId: String) {
        val photocard = dataManager.getSingleObjFromDb(Photocard::class.java, id)

        dataManager.getPhotocardFromNet(id, ownerId, getUpdated(photocard).toString())
                .subscribeWith(object : ErrorObserver<Photocard>() {
                    override fun onNext(it: Photocard) {
                        dataManager.saveToDB(it)
                    }
                })
    }

    override fun addToFavorite(id: String): Observable<Unit> {
        return dataManager.addToFavorite(id)
                .flatMap {
                    if (it.success) {
                        getFavoriteAlbum()
                    } else {
                        Observable.error(Exception("add to favorite fail"))
                    }
                }
                .map {
                    it.photocards.add(dataManager.getSingleObjFromDb(Photocard::class.java, id))
                    dataManager.saveToDB(it)
                }
    }

    override fun removeFromFavorite(id: String): Observable<Unit> {
        return dataManager.removeFromFavorite(id)
                .flatMap { getFavoriteAlbum() }
                .map {
                    it.photocards.removeAll { it.id == id }
                    dataManager.saveToDB(it)
                }
    }


    override fun isFavorite(id: String): Single<Boolean> {
        if (!dataManager.isUserAuth()) return Single.just(false)

        return getFavoriteAlbum()
                .flatMap { Observable.fromIterable(it.photocards) }
                .map { it.id }
                .contains(id)
    }

    private var favAlbumId: String? = null

    private fun getFavoriteAlbum(): Observable<Album> {
        return if (favAlbumId == null) {
            findFavAlbum()
        } else {
            dataManager.getObjectFromDb(Album::class.java, favAlbumId as String)
        }
    }

    private fun findFavAlbum(): Observable<Album> {
        val query = listOf(
                Query("owner", RealmOperator.EQUALTO, dataManager.getProfileId()),
                Query("isFavorite", RealmOperator.EQUALTO, true)
        )
        return dataManager.search(Album::class.java, query, "id")
                .map { it[0] }
                .doOnNext { favAlbumId = it.id }
    }
}
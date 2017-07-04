package io.github.vladimirmi.photon.features.photocard

import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.SuccessRes
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.models.realm.User
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.*

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
        val updated = user?.updated ?: Date(0)
        dataManager.getUserFromNet(id, updated.toString())
                .subscribeOn(Schedulers.io())
                .subscribe { dataManager.saveToDB(it) }
    }

    override fun getPhotocard(id: String, ownerId: String): Observable<Photocard> {
        updatePhotocard(id, ownerId)
        return dataManager.getObjectFromDb(Photocard::class.java, id)
    }

    private fun updatePhotocard(id: String, ownerId: String) {
        val photocard = dataManager.getSingleObjFromDb(Photocard::class.java, id)
        val updated = photocard?.updated ?: Date(0)
        dataManager.getPhotocardFromNet(id, ownerId, updated.toString())
                .subscribeOn(Schedulers.io())
                .subscribe { dataManager.saveToDB(it) }
    }

    override fun addToFavorite(photocard: Photocard): Observable<SuccessRes> {
        return dataManager.addToFavorite(photocard.id)
                .doOnComplete {
                    val user = dataManager.getSingleObjFromDb(User::class.java, dataManager.getProfileId())
                    if (user != null) {
                        user.updated = Date(0)
                        dataManager.saveToDB(user)
                    }
                }
    }
}
package io.github.vladimirmi.photon.features.photocard

import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.Photocard
import io.github.vladimirmi.photon.data.models.User
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

/**
 * Created by Vladimir Mikhalev 14.06.2017.
 */

class PhotocardModel(private val dataManager: DataManager) : IPhotocardModel {

    override fun getUser(id: String): Observable<User> {
        updateUser(id)
        return dataManager.getObjectFromDb(User::class.java, id)
    }

    private fun updateUser(id: String) {
        dataManager.getUserFromNet(id)
                .subscribeOn(Schedulers.io())
                .subscribe {
                    it.id = id  //todo workaround on miss id in response
                    dataManager.saveToDB(it)
                }
    }

    override fun getPhotocard(id: String, ownerId: String): Observable<Photocard> {
        updatePhotocard(id, ownerId)
        return dataManager.getObjectFromDb(Photocard::class.java, id)
    }

    private fun updatePhotocard(id: String, ownerId: String) {
        dataManager.getPhotocardFromNet(id, ownerId)
                .subscribeOn(Schedulers.io())
                .subscribe { dataManager.saveToDB(it) }
    }

}
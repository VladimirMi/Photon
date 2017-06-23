package io.github.vladimirmi.photon.features.profile

import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.User
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class ProfileModel(private val dataManager: DataManager) : IProfileModel {

    override fun isUserAuth(): Boolean {
        return dataManager.isUserAuth()
    }

    override fun getProfile(): Observable<User> {
        val id = dataManager.getProfileId()
        return getUser(id)
    }

    override fun getUser(userId: String): Observable<User> {
        updateUser(userId)
        return dataManager.getObjectFromDb(User::class.java, userId)
                .distinctUntilChanged()
    }

    private fun updateUser(id: String) {
        dataManager.getUserFromNet(id)
                .subscribeOn(Schedulers.io())
                .subscribe {
                    it.id = id  //todo a workaround on miss id in the response
                    dataManager.saveToDB(it)
                }
    }
}
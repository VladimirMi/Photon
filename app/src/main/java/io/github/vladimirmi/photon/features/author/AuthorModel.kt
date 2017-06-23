package io.github.vladimirmi.photon.features.author

import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.User
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class AuthorModel(private val dataManager: DataManager) : IAuthorModel {

    override fun getUser(userId: String): Observable<User> {
        updateUser(userId)
        return dataManager.getObjectFromDb(User::class.java, userId)
                .distinctUntilChanged()
                .doOnDispose { Timber.e("getUser: dispose") }
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
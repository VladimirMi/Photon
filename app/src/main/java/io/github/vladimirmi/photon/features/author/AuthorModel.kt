package io.github.vladimirmi.photon.features.author

import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.realm.User
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.*

class AuthorModel(private val dataManager: DataManager) : IAuthorModel {

    override fun getUser(userId: String): Observable<User> {
        updateUser(userId)
        return dataManager.getObjectFromDb(User::class.java, userId)
    }

    private fun updateUser(id: String) {
        val user = dataManager.getSingleObjFromDb(User::class.java, id)
        var updated = user?.updated ?: Date(0)
        val hour = 3600_000L // milliseconds
        if (updated.time != 0L && Date().time - hour > updated.time) updated = Date(0)

        dataManager.getUserFromNet(id, updated.toString())
                .subscribeOn(Schedulers.io())
                .subscribe { dataManager.saveToDB(it) }
    }
}
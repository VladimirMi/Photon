package io.github.vladimirmi.photon.features.profile

import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.NewAlbumReq
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.User
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

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

    }

    private fun updateUser(id: String) {
        val user = dataManager.getSingleFromDb(User::class.java, id)
        val updated = user?.updated ?: Date(0)
        dataManager.getUserFromNet(id, updated.toString())
                .subscribeOn(Schedulers.io())
                .subscribe { dataManager.saveToDB(it) }
    }

    override fun createAlbum(newAlbumReq: NewAlbumReq): Observable<Album> {
        newAlbumReq.owner = dataManager.getProfileId()
        return dataManager.createAlbum(newAlbumReq)
                .doOnNext { dataManager.saveToDB(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }
}
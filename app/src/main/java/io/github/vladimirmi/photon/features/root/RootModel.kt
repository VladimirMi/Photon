package io.github.vladimirmi.photon.features.root

import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.data.models.req.SignInReq
import io.github.vladimirmi.photon.data.models.req.SignUpReq
import io.github.vladimirmi.photon.utils.ioToMain
import io.reactivex.Single
import java.util.concurrent.TimeUnit

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

class RootModel(private val dataManager: DataManager) : IRootModel {

    override fun isUserAuth() = dataManager.isUserAuth()

    override fun register(req: SignUpReq): Single<Unit> {
        return dataManager.signUp(req)
                .map { saveUser(it) }
                .delay(1000, TimeUnit.MILLISECONDS)
                .ioToMain()
    }

    override fun login(req: SignInReq): Single<Unit> {
        return dataManager.signIn(req)
                .map { saveUser(it) }
                .delay(1000, TimeUnit.MILLISECONDS)
                .ioToMain()
    }

    override fun logout() = dataManager.logout()

    override fun isNetAvail() = dataManager.checkNetAvail()

    private fun saveUser(user: User) {
        dataManager.saveFromServer(user)
        dataManager.saveUserId(user.id)
        dataManager.saveUserToken(user.token)
        dataManager.saveUserFavAlbumId(user.albums.find { it.isFavorite }!!.id)
    }

    override fun syncProfile() = dataManager.syncProfile()
}

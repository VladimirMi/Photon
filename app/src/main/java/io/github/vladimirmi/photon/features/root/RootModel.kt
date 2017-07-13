package io.github.vladimirmi.photon.features.root

import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.SignInReq
import io.github.vladimirmi.photon.data.models.SignUpReq
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.utils.ioToMain
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

class RootModel(private val dataManager: DataManager) : IRootModel {

    override fun isUserAuth() = dataManager.isUserAuth()

    override fun register(req: SignUpReq): Observable<User> {
        return dataManager.signUp(req)
                .doOnNext { saveUser(it) }
                .delay(1000, TimeUnit.MILLISECONDS, true)
                .ioToMain()
    }

    override fun login(req: SignInReq): Observable<User> {
        return dataManager.signIn(req)
                .doOnNext { saveUser(it) }
                .delay(1000, TimeUnit.MILLISECONDS, true)
                .ioToMain()
    }

    override fun logout() = dataManager.logout()

    private fun saveUser(user: User) {
        dataManager.saveToDB(user)
        dataManager.saveUserId(user.id)
        dataManager.saveUserToken(user.token)
    }
}

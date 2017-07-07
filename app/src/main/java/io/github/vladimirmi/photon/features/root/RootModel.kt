package io.github.vladimirmi.photon.features.root

import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.SignInReq
import io.github.vladimirmi.photon.data.models.SignUpReq
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.utils.AppConfig
import io.github.vladimirmi.photon.utils.ErrorObserver
import io.github.vladimirmi.photon.utils.ioToMain
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

class RootModel(private val dataManager: DataManager) : IRootModel {
    override fun updatePhotocards(): Observable<Int> {
//        val updateObs = Observable.interval(0, 500, TimeUnit.MILLISECONDS)
//                .flatMap {
//                    dataManager.getPhotocardsFromNet(limit = 60,
//                            offset = (it * 60).toInt())
//                            .toObservable()
//                }
//                .flatMap { save(it) }
//                .share()

        val updateObs = Observable.defer { dataManager.getPhotocardsFromNet(limit = 60, offset = 0) }
                .flatMap { save(it) }
                .share()

        Observable.interval(AppConfig.PHOTOCARD_UPDATE_DELAY, TimeUnit.MINUTES)
                .withLatestFrom(dataManager.isNetworkAvailable(),
                        BiFunction { _: Long, netAvail: Boolean -> netAvail })
                .filter { it }
                .flatMap { updateObs }
                .subscribeWith(ErrorObserver())

        return updateObs
    }

    private fun save(photocards: List<Photocard>): Observable<Int> {
        var count = 0
        return Observable.fromIterable(photocards)
                .doOnNext { dataManager.saveToDB(it.withId()) }
                .flatMap { Observable.just(++count) }
    }

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

    private fun saveUser(it: User) {
        dataManager.saveToDB(it)
        dataManager.saveUserId(it.id)
        dataManager.saveUserToken(it.token)
    }
}

package io.github.vladimirmi.photon.features.splash

import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.utils.ioToMain
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

class SplashModel(val dataManager: DataManager) : ISplashModel {

    @Suppress("SimplifyBooleanWithConstants")
    override fun updateLimitPhotoCards(limit: Int): Observable<Int> {
        return dataManager.isNetworkAvailable()
                .filter { it != false }
                .firstOrError().toObservable()
                .flatMap { dataManager.getPhotocardsFromNet(0, limit) }
                .doOnNext { it.forEach { dataManager.saveToDB(it) } }
                .map { it.size }
                .firstOrError().toObservable()
    }

    override fun dbIsNotEmpty(): Single<Boolean> {
        return dataManager.getListFromDb(Photocard::class.java)
                .map { it.isNotEmpty() }
                .firstOrError()
                .ioToMain()
    }
}

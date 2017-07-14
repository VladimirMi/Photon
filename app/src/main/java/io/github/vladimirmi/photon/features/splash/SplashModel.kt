package io.github.vladimirmi.photon.features.splash

import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.reactivex.Observable

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

class SplashModel(val dataManager: DataManager) : ISplashModel {

    @Suppress("SimplifyBooleanWithConstants")
    override fun updateLimitPhotoCards(limit: Int, minDelay: Long): Observable<List<Photocard>> {
        return dataManager.isNetworkAvailable()
                .filter { it != false }
                .firstOrError().toObservable()
                .flatMap { dataManager.getPhotocardsFromNet(0, limit) }
                .doOnNext { it.forEach { dataManager.saveToDB(it) } }
                .firstOrError().toObservable()
    }

    override fun dbIsNotEmpty(): Boolean {
        return dataManager.getListFromDb(Photocard::class.java, async = false)
                .blockingFirst().isNotEmpty()
    }
}

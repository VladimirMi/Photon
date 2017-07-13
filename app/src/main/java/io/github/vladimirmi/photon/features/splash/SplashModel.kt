package io.github.vladimirmi.photon.features.splash

import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.utils.ioToMain
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

class SplashModel(val dataManager: DataManager) : ISplashModel {

    @Suppress("SimplifyBooleanWithConstants")
    override fun updateLimitPhotoCards(limit: Int, minDelay: Long): Observable<Boolean> {
        val updateObs = dataManager.isNetworkAvailable()
                .filter { it != false }
                .firstOrError().toObservable()
                .flatMap { dataManager.getPhotocardsFromNet(0, limit) }
                .doOnNext { it.forEach { dataManager.saveToDB(it) } }
                .map { true }
                .firstOrError().toObservable()

        val delayObs = Observable.interval(minDelay, TimeUnit.MILLISECONDS)
                .take(2).map { false }

        return Observable.mergeDelayError(updateObs, delayObs)
                .ioToMain()
    }

    override fun dbIsNotEmpty(): Boolean {
        return dataManager.getListFromDb(Photocard::class.java, async = false)
                .blockingFirst().isNotEmpty()
    }
}

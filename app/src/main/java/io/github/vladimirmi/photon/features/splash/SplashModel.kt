package io.github.vladimirmi.photon.features.splash

import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.utils.ioToMain
import io.github.vladimirmi.photon.utils.unit
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

class SplashModel(val dataManager: DataManager) : ISplashModel {

    override fun updateAll(limit: Int): Completable {
        return dataManager.isNetworkAvailable()
                .filter { it }
                .flatMap { Observable.mergeDelayError(updatePhotocards(limit), updateProfile()) }
                .ignoreElements()
                .ioToMain()
    }

    override fun dbIsNotEmpty(): Single<Boolean> {
        return dataManager.getListFromDb(Photocard::class.java)
                .map { it.isNotEmpty() }
                .firstOrError()
                .ioToMain()
    }

    private fun updatePhotocards(limit: Int): Observable<Unit> {
        return dataManager.getPhotocardsFromNet(0, limit)
                .doOnNext { it.forEach { dataManager.saveToDB(it) } }
                .unit()
    }

    private fun updateProfile(): Observable<Unit> {
        return if (dataManager.getProfileId().isNotEmpty()) {
            dataManager.getUserFromNet(dataManager.getProfileId())
                    .doOnNext { dataManager.saveToDB(it) }
                    .unit()
        } else {
            Observable.empty()
        }
    }
}

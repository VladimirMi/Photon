package io.github.vladimirmi.photon.features.splash

import io.github.vladimirmi.photon.data.managers.Cache
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.utils.ioToMain
import io.github.vladimirmi.photon.utils.unit
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

class SplashModel(val dataManager: DataManager, val cache: Cache) : ISplashModel {

    @Suppress("SimplifyBooleanWithConstants")
    override fun updateAll(limit: Int): Observable<Unit> {
        return dataManager.isNetworkAvailable()
                .filter { it != false }
                .firstOrError().toObservable()
                .flatMap { Observable.merge(updatePhotocards(limit), updateProfile()) }
                .unit()
                .ioToMain()
    }

    override fun dbIsNotEmpty(): Single<Boolean> {
        return dataManager.getListFromDb(Photocard::class.java)
                .map { it.isNotEmpty() }
                .firstOrError()
                .ioToMain()
    }

    fun updatePhotocards(limit: Int): Observable<Unit> {
        return dataManager.getPhotocardsFromNet(0, limit)
                .doOnNext { it.forEach { dataManager.saveToDB(it) } }
                .doOnNext { cache.cachePhotos(it) }
                .unit()
    }

    fun updateProfile(): Observable<Unit> {
        return if (dataManager.getProfileId().isNotEmpty()) {
            dataManager.getUserFromNet(dataManager.getProfileId(), Date(0).toString())
                    .doOnNext { dataManager.saveToDB(it) }
                    .doOnNext { cache.cacheUser(it) }
                    .unit()
        } else {
            Observable.empty()
        }
    }
}

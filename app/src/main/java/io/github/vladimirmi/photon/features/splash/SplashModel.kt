package io.github.vladimirmi.photon.features.splash

import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.features.root.IRootModel
import io.github.vladimirmi.photon.utils.ioToMain
import io.reactivex.Observable
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

class SplashModel(val dataManager: DataManager, val rootModel: IRootModel) : ISplashModel {

    override fun updateLimitPhotoCards(limit: Int, minDelay: Long): Observable<Any> {  // delay in milliseconds
        return Observable.merge(Observable.timer(minDelay, TimeUnit.MILLISECONDS)
                .doOnComplete { Timber.e("timer complete") },
                rootModel.updatePhotocards()
                        .doOnComplete { Timber.e("update complete") }
                        .filter { i -> i < limit })
                .doOnComplete { Timber.e("load complete") }
                .ioToMain()
    }

    override fun dbIsNotEmpty(): Boolean {
        return dataManager.getListFromDb(Photocard::class.java, "id").blockingFirst().isNotEmpty()
    }
}

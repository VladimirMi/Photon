package io.github.vladimirmi.photon.features.splash

import io.github.vladimirmi.photon.features.root.IRootModel
import io.reactivex.Observable
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

class SplashModel(private val rootModel: IRootModel) : ISplashModel {

    override fun updateLimitPhotoCards(limit: Int, minDelay: Long): Observable<Any> {  // delay in milliseconds
        return Observable.merge(Observable.timer(minDelay, TimeUnit.MILLISECONDS)
                .doOnComplete { Timber.e("timer complete") },
                rootModel.updatePhotoCards()
                        .doOnComplete { Timber.e("update complete") }
                        .filter { i -> i < limit })
                .doOnComplete { Timber.e("load complete") }
    }
}

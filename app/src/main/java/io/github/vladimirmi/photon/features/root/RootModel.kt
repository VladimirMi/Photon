package io.github.vladimirmi.photon.features.root

import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.Photocard
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

class RootModel(private val dataManager: DataManager) : IRootModel {
    override fun updatePhotoCards(): Observable<Int> {
//        val updateObs = Observable.interval(0, 500, TimeUnit.MILLISECONDS)
//                .flatMap {
//                    dataManager.getPhotocardsFromNet(limit = 60,
//                            offset = (it * 60).toInt())
//                            .toObservable()
//                }
//                .flatMap { save(it) }
//                .share()
        val updateObs = dataManager.getPhotocardsFromNet(limit = 60, offset = 0)
                .flatMap { save(it) }
                .share()

        Observable.interval(5, TimeUnit.MINUTES)
                .withLatestFrom(dataManager.isNetworkAvailable(),
                        BiFunction { _: Long, netAvail: Boolean -> netAvail })
                .filter { it == true }
                .flatMap { updateObs }
                .subscribe()

        return updateObs
    }

    private fun save(photocards: List<Photocard>): Observable<Int> {
        var count = 0
        return Observable.fromIterable(photocards)
                .doOnNext {
                    Timber.e("save $it")
                    dataManager.saveToDB(it.withId())
                }
                .flatMap { Observable.just(++count) }
    }

    override fun isUserAuth() = dataManager.isUserAuth()
}

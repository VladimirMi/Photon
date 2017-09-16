package io.github.vladimirmi.photon.presentation.splash

import io.github.vladimirmi.photon.core.Interactor
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

interface SplashInteractor : Interactor {
    fun updateAll(limit: Int): Completable
    fun dbIsNotEmpty(): Single<Boolean>
}

package io.github.vladimirmi.photon.presentation.root

import io.github.vladimirmi.photon.core.Interactor
import io.github.vladimirmi.photon.data.models.req.SignInReq
import io.github.vladimirmi.photon.data.models.req.SignUpReq
import io.reactivex.Completable

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

interface RootInteractor : Interactor {
    fun isUserAuth(): Boolean
    fun logout()
    fun login(req: SignInReq): Completable
    fun register(req: SignUpReq): Completable
    fun isNetAvail(): Boolean
}

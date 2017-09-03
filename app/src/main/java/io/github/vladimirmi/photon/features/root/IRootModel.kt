package io.github.vladimirmi.photon.features.root

import io.github.vladimirmi.photon.core.IModel
import io.github.vladimirmi.photon.data.models.req.SignInReq
import io.github.vladimirmi.photon.data.models.req.SignUpReq
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

interface IRootModel : IModel {
    fun isUserAuth(): Boolean
    fun logout()
    fun login(req: SignInReq): Single<Unit>
    fun register(req: SignUpReq): Single<Unit>
    fun isNetAvail(): Boolean
    fun syncProfile(): Completable
}

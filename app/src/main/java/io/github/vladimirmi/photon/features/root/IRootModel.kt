package io.github.vladimirmi.photon.features.root

import io.github.vladimirmi.photon.core.IModel
import io.github.vladimirmi.photon.data.models.SignInReq
import io.github.vladimirmi.photon.data.models.SignUpReq
import io.reactivex.Observable

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

interface IRootModel : IModel {
    fun isUserAuth(): Boolean
    fun logout()
    fun login(req: SignInReq): Observable<Unit>
    fun register(req: SignUpReq): Observable<Unit>
    fun isNetAvail(): Boolean
}

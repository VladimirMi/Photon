package io.github.vladimirmi.photon.data.models.realm.extensions

import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.data.models.req.ProfileEditReq
import io.github.vladimirmi.photon.di.DaggerService

/**
 * Created by Vladimir Mikhalev 03.09.2017.
 */

val User.dataManager: DataManager
    get() = DaggerService.appComponent.dataManager()

fun User.edit(request: ProfileEditReq) {
    login = request.login
    name = request.name
    avatar = request.avatar
    sync = false
    dataManager.save(this)
}
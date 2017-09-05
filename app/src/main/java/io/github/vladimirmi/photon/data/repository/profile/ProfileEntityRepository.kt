package io.github.vladimirmi.photon.data.repository.profile

import io.github.vladimirmi.photon.data.managers.RealmManager
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.data.models.req.ProfileEditReq
import io.github.vladimirmi.photon.data.repository.BaseEntityRepository

/**
 * Created by Vladimir Mikhalev 05.09.2017.
 */

open class ProfileEntityRepository(realmManager: RealmManager)
    : BaseEntityRepository(realmManager) {

    fun User.edit(request: ProfileEditReq) {
        login = request.login
        name = request.name
        avatar = request.avatar
        sync = false
        save(this)
    }
}
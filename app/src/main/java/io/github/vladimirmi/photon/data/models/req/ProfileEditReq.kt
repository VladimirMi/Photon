package io.github.vladimirmi.photon.data.models.req

import io.github.vladimirmi.photon.data.models.realm.User
import java.io.Serializable

/**
 * Created by Vladimir Mikhalev 29.06.2017.
 */

class ProfileEditReq(val user: User) : Serializable {
    val name = user.name
    val login = user.login
    var avatar = user.avatar
    @Transient
    var avatarChanged = !user.avatar.startsWith("http")
}
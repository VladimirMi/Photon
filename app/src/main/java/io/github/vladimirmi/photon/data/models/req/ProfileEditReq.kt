package io.github.vladimirmi.photon.data.models.req

import io.github.vladimirmi.photon.data.models.realm.User
import java.io.Serializable

/**
 * Created by Vladimir Mikhalev 29.06.2017.
 */

class ProfileEditReq(val name: String,
                     val login: String,
                     var avatar: String,
                     @field:Transient val avatarChanged: Boolean) : Serializable {


    companion object {
        fun fromProfile(profile: User) = with(profile) {
            ProfileEditReq(name, login, avatar, !avatar.startsWith("http"))
        }
    }
}
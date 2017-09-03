package io.github.vladimirmi.photon.data.models.req

import io.github.vladimirmi.photon.data.models.realm.User

/**
 * Created by Vladimir Mikhalev 29.06.2017.
 */

data class ProfileEditReq(
        val name: String,
        val login: String,
        val avatar: String) {

    companion object {
        fun from(profile: User) = with(profile) {
            ProfileEditReq(name, login, avatar)
        }
    }
}
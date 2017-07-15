package io.github.vladimirmi.photon.data.models.dto

import io.github.vladimirmi.photon.data.models.realm.User

/**
 * Created by Vladimir Mikhalev 15.07.2017.
 */


class UserDto(user: User) {
    val id = user.id
    val name = user.name
    val login = user.login
    val avatar = user.avatar
    val albums = ArrayList<AlbumDto>()

    init {
        user.albums.forEach { if (it.active) albums.add(AlbumDto(it)) }
    }
}
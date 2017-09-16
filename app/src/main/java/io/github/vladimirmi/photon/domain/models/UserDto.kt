package io.github.vladimirmi.photon.domain.models

import io.github.vladimirmi.photon.data.models.realm.User

/**
 * Created by Vladimir Mikhalev 15.07.2017.
 */


data class UserDto(val id: String = "",
                   val name: String = "",
                   val login: String = "",
                   val avatar: String = "",
                   val albums: List<AlbumDto> = ArrayList()) {

    constructor(user: User) : this(
            id = user.id,
            name = user.name,
            login = user.login,
            avatar = user.avatar,
            albums = user.albums.map { AlbumDto(it) }
    )
}
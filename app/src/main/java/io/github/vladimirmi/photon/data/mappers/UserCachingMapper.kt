package io.github.vladimirmi.photon.data.mappers

import io.github.vladimirmi.photon.data.models.dto.UserDto
import io.github.vladimirmi.photon.data.models.realm.User

/**
 * Created by Vladimir Mikhalev 05.09.2017.
 */

class UserCachingMapper : BaseCachingMapper<User, UserDto>() {
    override fun map(it: User) = UserDto(it)
}



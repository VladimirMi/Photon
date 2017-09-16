package io.github.vladimirmi.photon.domain.mappers

import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.domain.models.UserDto

/**
 * Created by Vladimir Mikhalev 05.09.2017.
 */

class UserCachingMapper : BaseCachingMapper<User, UserDto>() {
    override fun map(it: User) = UserDto(it)
}



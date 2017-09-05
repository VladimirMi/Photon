package io.github.vladimirmi.photon.data.mappers

import io.github.vladimirmi.photon.data.models.dto.PhotocardDto
import io.github.vladimirmi.photon.data.models.realm.Photocard

/**
 * Created by Vladimir Mikhalev 05.09.2017.
 */


class PhotocardCachingMapper : BaseCachingMapper<Photocard, PhotocardDto>() {
    override fun map(it: Photocard) = PhotocardDto(it)
}
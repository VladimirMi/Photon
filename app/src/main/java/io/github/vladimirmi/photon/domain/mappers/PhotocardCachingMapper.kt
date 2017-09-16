package io.github.vladimirmi.photon.domain.mappers

import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.domain.models.PhotocardDto

/**
 * Created by Vladimir Mikhalev 05.09.2017.
 */


class PhotocardCachingMapper : BaseCachingMapper<Photocard, PhotocardDto>() {
    override fun map(it: Photocard) = PhotocardDto(it)
}
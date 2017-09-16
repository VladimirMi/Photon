package io.github.vladimirmi.photon.domain.mappers

import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.domain.models.AlbumDto

/**
 * Created by Vladimir Mikhalev 05.09.2017.
 */

class AlbumCachingMapper : BaseCachingMapper<Album, AlbumDto>() {
    override fun map(it: Album) = AlbumDto(it)
}
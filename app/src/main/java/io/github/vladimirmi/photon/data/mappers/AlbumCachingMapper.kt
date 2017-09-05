package io.github.vladimirmi.photon.data.mappers

import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.realm.Album

/**
 * Created by Vladimir Mikhalev 05.09.2017.
 */

class AlbumCachingMapper : BaseCachingMapper<Album, AlbumDto>() {
    override fun map(it: Album) = AlbumDto(it)
}
package io.github.vladimirmi.photon.data.models.dto

import io.github.vladimirmi.photon.data.models.realm.Album

/**
 * Created by Vladimir Mikhalev 15.07.2017.
 */

class AlbumDto(album: Album) {
    val id = album.id
    val owner = album.owner
    val title = album.title
    val description = album.description
    val isFavorite = album.isFavorite
    val photocards = ArrayList<PhotocardDto>()

    init {
        album.photocards.forEach { if (it.active) photocards.add(PhotocardDto(it)) }
    }
}
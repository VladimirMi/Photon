package io.github.vladimirmi.photon.domain.models

import io.github.vladimirmi.photon.data.models.realm.Album

/**
 * Created by Vladimir Mikhalev 15.07.2017.
 */

class AlbumDto(val id: String = "",
               val owner: String = "",
               val title: String = "",
               val description: String = "",
               val isFavorite: Boolean = false,
               val photocards: List<PhotocardDto> = ArrayList()) {

    constructor(album: Album) : this(
            album.id,
            album.owner,
            album.title,
            album.description,
            album.isFavorite,
            album.photocards.map { PhotocardDto(it) }
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as AlbumDto

        if (id != other.id) return false
        if (owner != other.owner) return false
        if (title != other.title) return false
        if (description != other.description) return false
        if (isFavorite != other.isFavorite) return false
        if (photocards != other.photocards) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + owner.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + isFavorite.hashCode()
        result = 31 * result + photocards.hashCode()
        return result
    }

}
package io.github.vladimirmi.photon.data.models.dto

import io.github.vladimirmi.photon.data.models.realm.Photocard

/**
 * Created by Vladimir Mikhalev 15.07.2017.
 */

class PhotocardDto(photocard: Photocard) {
    val id = photocard.id
    val title = photocard.title
    val photo = photocard.photo
    val owner = photocard.owner
    val views = photocard.views
    val favorits = photocard.favorits
    val tags = ArrayList<String>()

    init {
        photocard.tags.forEach { tags.add(it.value) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as PhotocardDto

        if (id != other.id) return false
        if (title != other.title) return false
        if (photo != other.photo) return false
        if (owner != other.owner) return false
        if (views != other.views) return false
        if (favorits != other.favorits) return false
        if (tags != other.tags) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + photo.hashCode()
        result = 31 * result + owner.hashCode()
        result = 31 * result + views
        result = 31 * result + favorits
        result = 31 * result + tags.hashCode()
        return result
    }
}
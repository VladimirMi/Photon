package io.github.vladimirmi.photon.data.models.dto

import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.models.realm.Tag

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
    val tags = ArrayList<TagDto>()

    init {
        photocard.tags.forEach { tags.add(TagDto(it)) }
    }
}

class TagDto(tag: Tag) {
    val value = tag.value
}
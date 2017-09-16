package io.github.vladimirmi.photon.data.models.req

import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Entity

/**
 * Created by Vladimir Mikhalev 23.06.2017.
 */

class AlbumNewReq(
        val id: String = Entity.tempId(),
        val title: String,
        val description: String
) {

    companion object {
        fun fromAlbum(album: Album) = with(album) {
            AlbumNewReq(id, title, description)
        }
    }
}
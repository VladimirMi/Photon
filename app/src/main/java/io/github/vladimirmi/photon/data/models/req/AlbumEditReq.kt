package io.github.vladimirmi.photon.data.models.req

import io.github.vladimirmi.photon.data.models.realm.Album
import java.io.Serializable

/**
 * Created by Vladimir Mikhalev 27.06.2017.
 */

data class AlbumEditReq(val id: String,
                        val title: String,
                        val description: String) : Serializable {

    companion object {
        fun fromAlbum(album: Album) = with(album) {
            AlbumEditReq(id, title, description)
        }
    }
}
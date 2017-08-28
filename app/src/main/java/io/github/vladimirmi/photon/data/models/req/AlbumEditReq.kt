package io.github.vladimirmi.photon.data.models.req

import io.github.vladimirmi.photon.data.models.realm.Album
import java.io.Serializable

/**
 * Created by Vladimir Mikhalev 27.06.2017.
 */

class AlbumEditReq(album: Album) : Serializable {

    var id = album.id
    val title = album.title
    val description = album.description
}
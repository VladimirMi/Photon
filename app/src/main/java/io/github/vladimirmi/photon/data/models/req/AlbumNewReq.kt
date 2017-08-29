package io.github.vladimirmi.photon.data.models.req

import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.di.DaggerService
import java.io.Serializable

/**
 * Created by Vladimir Mikhalev 23.06.2017.
 */

class AlbumNewReq(
        val id: String,
        val title: String,
        val description: String,
        val owner: String = DaggerService.appComponent.dataManager().getProfileId()
) : Serializable {

    companion object {
        fun fromAlbum(album: Album) = with(album) {
            AlbumNewReq(id, title, description)
        }
    }
}
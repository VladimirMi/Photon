package io.github.vladimirmi.photon.data.models.req

import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.di.DaggerService
import java.io.Serializable

/**
 * Created by Vladimir Mikhalev 23.06.2017.
 */

class AlbumNewReq(album: Album) : Serializable {

    val id = album.id
    val owner = DaggerService.appComponent.dataManager().getProfileId()
    val title = album.title
    val description = album.description
}
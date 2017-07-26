package io.github.vladimirmi.photon.data.models.req

import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.di.DaggerService
import java.io.Serializable
import java.util.*

/**
 * Created by Vladimir Mikhalev 23.06.2017.
 */

class NewAlbumReq(val title: String,
                  val description: String) : Serializable {

    val id: String = Album.TEMP + UUID.randomUUID().toString()
    val owner: String = DaggerService.appComponent.dataManager().getProfileId()
}
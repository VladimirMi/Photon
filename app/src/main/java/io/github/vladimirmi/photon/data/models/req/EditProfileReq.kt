package io.github.vladimirmi.photon.data.models.req

import io.github.vladimirmi.photon.di.DaggerService
import java.io.Serializable

/**
 * Created by Vladimir Mikhalev 29.06.2017.
 */

class EditProfileReq(val name: String,
                     val login: String,
                     var avatar: String = "") : Serializable {
    val id = DaggerService.appComponent.dataManager().getProfileId()

    var avatarChanged = false
}
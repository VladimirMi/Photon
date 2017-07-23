package io.github.vladimirmi.photon.data.models.req

import java.io.Serializable

/**
 * Created by Vladimir Mikhalev 29.06.2017.
 */

class EditProfileReq(var id: String = "",
                     var name: String = "",
                     var login: String = "",
                     var avatar: String = "") : Serializable
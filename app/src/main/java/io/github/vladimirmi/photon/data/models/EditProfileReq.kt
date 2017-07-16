package io.github.vladimirmi.photon.data.models

import java.io.Serializable

/**
 * Created by Vladimir Mikhalev 29.06.2017.
 */

class EditProfileReq(@field:Transient var id: String = "",
                     var name: String = "",
                     var login: String = "",
                     var avatar: String = "") : Serializable
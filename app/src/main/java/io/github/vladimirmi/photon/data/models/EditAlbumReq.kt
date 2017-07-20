package io.github.vladimirmi.photon.data.models

import java.io.Serializable

/**
 * Created by Vladimir Mikhalev 27.06.2017.
 */

class EditAlbumReq(var id: String = "",
                   val title: String = "",
                   val description: String = "") : Serializable
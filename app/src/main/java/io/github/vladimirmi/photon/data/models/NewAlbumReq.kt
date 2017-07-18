package io.github.vladimirmi.photon.data.models

import java.io.Serializable

/**
 * Created by Vladimir Mikhalev 23.06.2017.
 */

class NewAlbumReq(@field:Transient var id: String = "",
                  var owner: String = "",
                  val title: String,
                  val description: String) : Serializable
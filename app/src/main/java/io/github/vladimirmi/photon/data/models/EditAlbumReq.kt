package io.github.vladimirmi.photon.data.models

/**
 * Created by Vladimir Mikhalev 27.06.2017.
 */

class EditAlbumReq(@field:Transient var id: String = "",
                   val title: String = "",
                   val description: String = "")
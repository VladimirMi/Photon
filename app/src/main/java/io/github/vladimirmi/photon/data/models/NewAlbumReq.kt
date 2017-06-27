package io.github.vladimirmi.photon.data.models

/**
 * Created by Vladimir Mikhalev 23.06.2017.
 */

class NewAlbumReq(var owner: String = "",
                  val title: String,
                  val description: String)
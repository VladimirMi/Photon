package io.github.vladimirmi.photon.presentation.newcard

import io.github.vladimirmi.photon.data.models.realm.Filter

/**
 * Created by Vladimir Mikhalev 29.07.2017.
 */

class NewCardScreenInfo {
    var returnToAlbum = false
    var currentPage = Page.INFO
    var photo = ""
    var title = ""
    var tag = ""
    val tags = ArrayList<String>()
    val filter = Filter()
    var album = ""
}
package io.github.vladimirmi.photon.data.managers.extensions

import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.models.realm.User

/**
 * Created by Vladimir Mikhalev 03.09.2017.
 */

fun DataManager.getProfile(): User = getDetachedObjFromDb(User::class.java, getProfileId())!!

fun DataManager.getAlbum(id: String): Album = getDetachedObjFromDb(Album::class.java, id)!!

fun DataManager.getPhotocard(id: String): Photocard =
        getDetachedObjFromDb(Photocard::class.java, id)!!
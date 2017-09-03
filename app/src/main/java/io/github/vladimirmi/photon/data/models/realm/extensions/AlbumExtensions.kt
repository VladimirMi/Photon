package io.github.vladimirmi.photon.data.models.realm.extensions

import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.managers.extensions.getPhotocard
import io.github.vladimirmi.photon.data.managers.extensions.getProfile
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.req.AlbumEditReq
import io.github.vladimirmi.photon.di.DaggerService

/**
 * Created by Vladimir Mikhalev 03.09.2017.
 */

val Album.dataManager: DataManager
    get() = DaggerService.appComponent.dataManager()

fun Album.create() {
    owner = dataManager.getProfileId()
    sync = false
    val profile = dataManager.getProfile()
    profile.albums.add(this)
    dataManager.save(profile)
}

fun Album.delete() {
    photocards.forEach { it.delete() }
    active = false
    sync = false
    dataManager.save(this)
}

fun Album.edit(request: AlbumEditReq) {
    title = request.title
    description = request.description
    sync = false
    dataManager.save(this)
}

fun Album.addFavorite(id: String) {
    photocards.add(dataManager.getPhotocard(id))
    sync = false
    dataManager.save(this)
}

fun Album.deleteFavorite(id: String) {
    photocards.removeAll { it.id == id }
    sync = false
    dataManager.save(this)
}
package io.github.vladimirmi.photon.data.models.realm.extensions

import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.managers.extensions.getAlbum
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.di.DaggerService
import timber.log.Timber

/**
 * Created by Vladimir Mikhalev 03.09.2017.
 */

val Photocard.dataManager: DataManager
    get() = DaggerService.appComponent.dataManager()

fun Photocard.create() {
    this.sync = false
    val album = dataManager.getAlbum(this.album)
    album.photocards.add(this)
    dataManager.save(album)
}

fun Photocard.delete() {
    with(this) {
        active = false
        sync = false
    }
    dataManager.save(this)
}

fun Photocard.addView() {
    Timber.e("addView: ")
    with(this) {
        views++
        sync = false
    }
    dataManager.save(this)
}
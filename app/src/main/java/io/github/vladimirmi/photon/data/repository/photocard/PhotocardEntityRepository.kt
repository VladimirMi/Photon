package io.github.vladimirmi.photon.data.repository.photocard

import io.github.vladimirmi.photon.data.managers.RealmManager
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.repository.BaseEntityRepository

/**
 * Created by Vladimir Mikhalev 05.09.2017.
 */

open class PhotocardEntityRepository(realmManager: RealmManager)
    : BaseEntityRepository(realmManager) {

    fun Photocard.create() {
        sync = false
        val album = getAlbum(album)
        album.photocards.add(this)
        save(album)
    }

    fun Photocard.delete() {
        active = false
        sync = false
        save(this)
    }

    fun Photocard.addView() {
        views++
        sync = false
        save(this)
    }
}
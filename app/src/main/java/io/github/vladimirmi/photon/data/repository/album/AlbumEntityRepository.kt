package io.github.vladimirmi.photon.data.repository.album

import io.github.vladimirmi.photon.data.managers.PreferencesManager
import io.github.vladimirmi.photon.data.managers.RealmManager
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.req.AlbumEditReq
import io.github.vladimirmi.photon.data.repository.BaseEntityRepository

/**
 * Created by Vladimir Mikhalev 05.09.2017.
 */

open class AlbumEntityRepository(realmManager: RealmManager,
                                 protected val preferencesManager: PreferencesManager)
    : BaseEntityRepository(realmManager) {

    fun Album.create() {
        owner = preferencesManager.getProfileId()
        val profile = getUser(preferencesManager.getProfileId())
        profile.albums.add(this)
        save(profile)
    }

    fun Album.delete() {
        active = false
        save(this)
    }

    fun Album.edit(request: AlbumEditReq) {
        title = request.title
        description = request.description
        save(this)
    }

    fun Album.addFavorite(id: String) {
        photocards.add(getPhotocard(id))
        save(this)
    }

    fun Album.deleteFavorite(id: String) {
        photocards.removeAll { it.id == id }
        save(this)
    }
}
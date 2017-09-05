package io.github.vladimirmi.photon.data.repository.album

import io.github.vladimirmi.photon.data.managers.PreferencesManager
import io.github.vladimirmi.photon.data.managers.RealmManager
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.data.models.req.AlbumEditReq
import io.github.vladimirmi.photon.data.repository.BaseEntityRepository

/**
 * Created by Vladimir Mikhalev 05.09.2017.
 */

open class AlbumEntityRepository(realmManager: RealmManager,
                                 protected val preferencesManager: PreferencesManager)
    : BaseEntityRepository(realmManager) {

    protected open fun getProfile(): User = getUser(preferencesManager.getProfileId())

    fun Album.create() {
        owner = preferencesManager.getProfileId()
        sync = false
        val profile = getProfile()
        profile.albums.add(this)
        save(profile)
    }

    fun Album.delete() {
        active = false
        sync = false
        save(this)
    }

    fun Album.edit(request: AlbumEditReq) {
        title = request.title
        description = request.description
        sync = false
        save(this)
    }

    fun Album.addFavorite(id: String) {
        photocards.add(getPhotocard(id))
        sync = false
        save(this)
    }

    fun Album.deleteFavorite(id: String) {
        photocards.removeAll { it.id == id }
        sync = false
        save(this)
    }
}
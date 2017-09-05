package io.github.vladimirmi.photon.data.repository

import io.github.vladimirmi.photon.data.managers.RealmManager
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.models.realm.Synchronizable
import io.github.vladimirmi.photon.data.models.realm.User
import io.realm.RealmObject

/**
 * Created by Vladimir Mikhalev 04.09.2017.
 */

open class BaseEntityRepository(protected val realmManager: RealmManager) {

    protected open fun getPhotocard(id: String): Photocard =
            realmManager.getUnmanagedObject(Photocard::class.java, id) ?: throw NoSuchElementException()

    protected open fun getAlbum(id: String): Album =
            realmManager.getUnmanagedObject(Album::class.java, id) ?: throw NoSuchElementException()

    protected open fun getUser(id: String): User =
            realmManager.getUnmanagedObject(User::class.java, id) ?: throw NoSuchElementException()

    protected fun saveFromNet(it: Synchronizable) {
        it.transform()?.let { save(it as RealmObject) }
    }

    protected fun saveFromNet(list: List<Synchronizable>) {
        save(list.mapNotNull { it.transform() as? RealmObject })
    }

    fun save(it: RealmObject) = realmManager.save(it)

    fun <T : RealmObject> save(list: List<T>) = realmManager.save(list)
}
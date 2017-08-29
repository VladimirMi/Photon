package io.github.vladimirmi.photon.data.managers

import io.github.vladimirmi.photon.data.models.dto.Cached
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.utils.*
import io.reactivex.Observable
import io.realm.Realm
import io.realm.RealmObject
import io.realm.Sort
import java.lang.UnsupportedOperationException

/**
 * Created by Vladimir Mikhalev 04.06.2017.
 */

class RealmManager(private val cache: Cache) {

    //todo SRP violation
    fun save(realmObject: RealmObject) {
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction { it.insertOrUpdate(realmObject) }
        realm.close()
        cache.cache(realmObject)
    }

    fun <T : RealmObject> save(list: List<T>) {
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction { it.insertOrUpdate(list) }
        realm.close()
        cache.cache(list)
    }

    fun saveFromNet(realmObject: RealmObject) {
        setupObject(realmObject)?.let { save(it) }
    }

    fun <T : RealmObject> saveFromNet(list: List<T>) {
        save(list.mapNotNull { setupObject(it) })
    }

    private fun setupObject(realmObject: RealmObject): RealmObject? {
        fun setupPhotocard(photocard: Photocard) = if (photocard.active && photocard.sync)
            photocard.apply {
                searchTag = title.toLowerCase()
                if (filters.id.isEmpty()) generateId()
            }
        else null

        fun setupAlbum(album: Album) = if (album.active && album.sync)
            album.apply {
                photocards.retainAll { it.active }
                photocards.forEachIndexed { index, photo -> photocards[index] = setupPhotocard(photo) }
            }
        else null

        fun setupUser(user: User) = if (user.active && user.sync)
            user.apply {
                albums.retainAll { it.active }
                albums.forEachIndexed { index, album -> albums[index] = setupAlbum(album) }
            }
        else null

        return when (realmObject) {
            is User -> realmObject.let { setupUser(it) }
            is Album -> realmObject.let { setupAlbum(it) }
            is Photocard -> realmObject.let { setupPhotocard(it) }
            else -> realmObject
        }
    }


    fun <T : RealmObject> getObject(clazz: Class<T>, id: String): Observable<T> {
        val query = listOf(Query("id", RealmOperator.EQUALTO, id))
        return search(clazz, query)
                .flatMap { if (it.isEmpty()) Observable.empty() else Observable.just(it.first()) }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : RealmObject, R : Cached> getCached(clazz: Class<T>, id: String): Observable<R> {
        fun cached() = when (clazz) {
            Album::class.java -> cache.album(id)
            Photocard::class.java -> cache.photocard(id)
            User::class.java -> cache.user(id)
            else -> throw UnsupportedOperationException()
        } as R?
        return Observable.merge(justOrEmpty(cached()),
                getObject(clazz, id).flatMap { justOrEmpty(cache.cache(it) as R?) })
    }

    fun <T : RealmObject> search(clazz: Class<T>,
                                 query: List<Query>? = null,
                                 sortBy: String? = null,
                                 order: Sort = Sort.ASCENDING,
                                 detach: Boolean = false): Observable<List<T>> {
        return asFlowable(detach) {
            it.where(clazz).prepareQuery(query)
                    .let {
                        if (sortBy != null) {
                            it.findAllSorted(sortBy, order)
                        } else {
                            it.findAll()
                        }
                    }
        }.toObservable()
    }


    fun <T : RealmObject> remove(clazz: Class<T>, id: String) {
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction {
            it.where(clazz).equalTo("id", id).findFirst()?.deleteFromRealm()
        }
        realm.close()
    }

    fun <T : RealmObject> getDetachedObject(java: Class<T>, id: String): T? {
        val realm = Realm.getDefaultInstance()
        val result = realm.where(java).equalTo("id", id).findFirst()?.let { realm.copyFromRealm(it) }
        realm.close()
        return result
    }

}

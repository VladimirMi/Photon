package io.github.vladimirmi.photon.data.managers

import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Changeable
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.utils.Query
import io.github.vladimirmi.photon.utils.RealmOperator
import io.github.vladimirmi.photon.utils.asFlowable
import io.github.vladimirmi.photon.utils.prepareQuery
import io.reactivex.Observable
import io.realm.Realm
import io.realm.RealmObject
import io.realm.Sort
import java.util.concurrent.atomic.AtomicReference

/**
 * Created by Vladimir Mikhalev 04.06.2017.
 */

class RealmManager {

    fun save(realmObject: RealmObject) {
        val obj = setupObject(realmObject) ?: return
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction { it.insertOrUpdate(obj) }
        realm.close()
    }

    private fun setupObject(realmObject: RealmObject): RealmObject? {
        fun setupPhotocard(photocard: Photocard) = if (photocard.active)
            photocard.apply { withId(); searchTag = title.toLowerCase() }
        else null

        fun setupAlbum(album: Album) = if (album.active)
            album.apply {
                photocards.retainAll { it.active }
                photocards.forEachIndexed { index, photo -> photocards[index] = setupPhotocard(photo) }
            }
        else null

        fun setupUser(user: User) = if (user.active)
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

    fun <T : RealmObject> search(clazz: Class<T>,
                                 query: List<Query>? = null,
                                 sortBy: String? = null,
                                 order: Sort = Sort.ASCENDING,
                                 mainThread: Boolean = false): Observable<List<T>> {

        return AtomicReference<Realm>().asFlowable(mainThread) {
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

    fun <T : RealmObject> removeAllNotActive(clazz: Class<T>) {
        if (!Changeable::class.java.isAssignableFrom(clazz)) return
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction {
            it.where(clazz).equalTo("active", false).findAll()?.deleteAllFromRealm()
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

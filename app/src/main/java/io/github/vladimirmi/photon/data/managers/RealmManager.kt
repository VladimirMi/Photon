package io.github.vladimirmi.photon.data.managers

import io.github.vladimirmi.photon.data.models.realm.Changeable
import io.github.vladimirmi.photon.utils.Query
import io.github.vladimirmi.photon.utils.RealmObjectFlowable
import io.github.vladimirmi.photon.utils.RealmResultFlowable
import io.github.vladimirmi.photon.utils.prepareQuery
import io.reactivex.Observable
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.Sort
import timber.log.Timber

/**
 * Created by Vladimir Mikhalev 04.06.2017.
 */

class RealmManager {

    private val mainRealm = Realm.getDefaultInstance()

    fun save(realmObject: RealmObject) {
        if (realmObject is Changeable && !realmObject.active) {
            remove(realmObject::class.java, realmObject.id)
            return
        }
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction { realm.insertOrUpdate(realmObject) }
        realm.close()
    }

    fun <T : RealmObject> getObject(clazz: Class<T>, id: String): Observable<T> {
        return RealmObjectFlowable.obsFrom(mainRealm
                .where(clazz)
                .equalTo("id", id)
                .findFirstAsync())
                .filter { it.isLoaded }
    }

    fun <T : RealmObject> get(clazz: Class<T>, id: String): Observable<T> {
        removeAllNotActive(clazz)
        return RealmResultFlowable.obsFrom(mainRealm
                .where(clazz)
                .equalTo("id", id)
                .findAllAsync())
                .filter { it.isLoaded }
                .flatMap {
                    if (it.isNotEmpty()) {
                        Observable.just(it[0])
                    } else {
                        Observable.empty()
                    }
                }
    }

    fun <T : RealmObject> search(clazz: Class<T>,
                                 query: List<Query>?,
                                 sortBy: String?,
                                 order: Sort,
                                 async: Boolean): Observable<RealmResults<T>> {
        removeAllNotActive(clazz)
        val realmQuery = mainRealm.prepareQuery(clazz, query)

        val result = realmQuery.let { if (async) it.findAllAsync() else it.findAll() }
                .let { if (sortBy != null) it.sort(sortBy, order) else it }

        return RealmResultFlowable.obsFrom(result)
                .filter { it.isLoaded }
    }

    fun <T : RealmObject> remove(clazz: Class<T>, id: String) {
        Timber.e("remove: ${clazz.simpleName} with id=$id")
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

    fun <T : RealmObject> getSingle(java: Class<T>, id: String): T? {
        val realm = Realm.getDefaultInstance()
        val result = realm.where(java).equalTo("id", id).findFirst()?.let { realm.copyFromRealm(it) }
        realm.close()
        return result
    }

}

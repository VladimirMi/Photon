package io.github.vladimirmi.photon.data.managers

import io.github.vladimirmi.photon.data.models.realm.Changeable
import io.github.vladimirmi.photon.utils.Query
import io.github.vladimirmi.photon.utils.RealmObjectFlowable
import io.github.vladimirmi.photon.utils.RealmResultFlowable
import io.reactivex.Observable
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.Sort
import timber.log.Timber

/**
 * Created by Vladimir Mikhalev 04.06.2017.
 */

class RealmManager(private val realm: Realm) {

    fun save(realmObject: RealmObject) {
        realm.executeTransaction { realm.insertOrUpdate(realmObject) }
    }

    fun <T : RealmObject> getObject(clazz: Class<T>, id: String): Observable<T> {
        return RealmObjectFlowable.obsFrom(realm
                .where(clazz)
                .equalTo("id", id)
                .findFirstAsync())
                .filter { it.isLoaded }
    }

    fun <T : RealmObject> get(clazz: Class<T>, id: String): Observable<T> {
        removeAllNotActive(clazz)
        return RealmResultFlowable.obsFrom(realm
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

    fun <T : RealmObject> search(clazz: Class<T>, query: List<Query>?, sortBy: String, order: Sort)
            : Observable<RealmResults<T>> {
        removeAllNotActive(clazz)
        var realmQuery = realm.where(clazz)

        query?.groupBy { it.fieldName }?.forEach { (_, list) ->
            Timber.e("search: group $list")
            realmQuery = realmQuery.beginGroup()
            list.forEachIndexed { idx, qry ->
                realmQuery = qry.applyTo(realmQuery)
                if (idx < list.size - 1) realmQuery = realmQuery.or()
            }
            realmQuery = realmQuery.endGroup()
        }

        return RealmResultFlowable.obsFrom(realmQuery
                .findAllSortedAsync(sortBy, order))
                .filter { it.isLoaded }
    }

    fun <T : RealmObject> remove(clazz: Class<T>, id: String) {
        Timber.e("remove: ${clazz.simpleName} with id=$id")
        realm.executeTransaction {
            it.where(clazz).equalTo("id", id).findFirst()?.deleteFromRealm()
        }
    }

    fun <T : RealmObject> removeAllNotActive(clazz: Class<T>) {
        if (!Changeable::class.java.isAssignableFrom(clazz)) return
        realm.executeTransaction {
            it.where(clazz).equalTo("active", false).findAll()?.deleteAllFromRealm()
        }
    }

    fun <T : RealmObject> getSingle(java: Class<T>, id: String): T? {
        return realm.where(java).equalTo("id", id).findFirst()
    }
}
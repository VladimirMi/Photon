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

class RealmManager {

    fun saveAsync(realmObject: RealmObject) {
        val realm = Realm.getDefaultInstance()
        realm.executeTransactionAsync(Realm.Transaction {
            realm.insertOrUpdate(realmObject)
        },
                Realm.Transaction.OnError { Timber.e("saveAsync: OnError $it") })
        realm.close()
    }

    fun save(realmObject: RealmObject) {
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction { realm.insertOrUpdate(realmObject) }
        realm.close()
    }

    fun <T : RealmObject> getObject(clazz: Class<T>, id: String): Observable<T> {
        val realm = Realm.getDefaultInstance()
        return RealmObjectFlowable.obsFrom(realm
                .where(clazz)
                .equalTo("id", id)
                .findFirstAsync())
                .filter { it.isLoaded }
                .map { realm.copyFromRealm(it) }
                .doFinally { realm.close() }
    }


    fun <T : RealmObject> get(clazz: Class<T>, id: String): Observable<T> {
        removeAllNotActive(clazz)
        val realm = Realm.getDefaultInstance()
        return RealmResultFlowable.obsFrom(realm
                .where(clazz)
                .equalTo("id", id)
                .findAllAsync())
                .filter { it.isLoaded }
                .flatMap {
                    if (it.isNotEmpty()) {
//                        Observable.just(realm.copyFromRealm(it[0]))
                        Observable.just(it[0])
                    } else {
                        Observable.empty()
                    }
                }
                .doFinally { realm.close() }
    }

    fun <T : RealmObject> search(clazz: Class<T>, query: List<Query>?, sortBy: String, order: Sort)
            : Observable<RealmResults<T>> {
        removeAllNotActive(clazz)
        val realm = Realm.getDefaultInstance()
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
                .doFinally { realm.close() }
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

    fun <T : RealmObject> getSingle(java: Class<T>, id: String): T? {
        val realm = Realm.getDefaultInstance()
        var result = realm.where(java).equalTo("id", id).findFirst()
        result = result?.let { realm.copyFromRealm(result) }
        realm.close()
        return result
    }
}
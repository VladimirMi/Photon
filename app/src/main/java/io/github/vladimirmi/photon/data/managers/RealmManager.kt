package io.github.vladimirmi.photon.data.managers

import io.github.vladimirmi.photon.data.managers.utils.Query
import io.github.vladimirmi.photon.data.managers.utils.apply
import io.github.vladimirmi.photon.data.managers.utils.asFlowable
import io.github.vladimirmi.photon.data.models.realm.Entity
import io.reactivex.Observable
import io.realm.Realm
import io.realm.RealmObject
import io.realm.Sort
import java.util.*

/**
 * Created by Vladimir Mikhalev 04.06.2017.
 */

class RealmManager {

    fun save(realmObject: RealmObject) {
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction { it.insertOrUpdate(realmObject) }
        realm.close()
    }

    fun <T : RealmObject> save(list: List<T>) {
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction { it.insertOrUpdate(list) }
        realm.close()
    }

    fun <T : RealmObject> getObject(clazz: Class<T>,
                                    id: String,
                                    managed: Boolean): Observable<T> {
        val query = listOf(Query("id", Query.Operator.EQUAL_TO, id))
        return search(clazz, query, null, Sort.ASCENDING, managed).flatMapIterable { it }
//                .flatMap { if (it.isEmpty()) Observable.empty() else Observable.just(it.first()) }
    }


    fun <T : RealmObject> getList(clazz: Class<T>,
                                  sortBy: String? = null,
                                  order: Sort = Sort.ASCENDING,
                                  managed: Boolean = true): Observable<List<T>> =
            search(clazz, null, sortBy, order, managed)


    fun <T : RealmObject> search(clazz: Class<T>,
                                 query: List<Query>?,
                                 sortBy: String? = null,
                                 order: Sort = Sort.ASCENDING,
                                 managed: Boolean = true): Observable<List<T>> {
        return asFlowable(managed) {
            it.where(clazz).apply(query)
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

    fun <T : RealmObject> getUnmanagedObject(java: Class<T>, id: String): T? {
        val realm = Realm.getDefaultInstance()
        val result = realm.where(java).equalTo("id", id).findFirst()?.let { realm.copyFromRealm(it) }
        realm.close()
        return result
    }


    fun <T : RealmObject> getLastUpdated(clazz: Class<T>, id: String): String {
        val obj = getUnmanagedObject(clazz, id) as? Entity
        return (obj?.updated ?: Date(0)).toString()
    }

}

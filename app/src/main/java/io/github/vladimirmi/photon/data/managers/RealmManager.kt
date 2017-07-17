package io.github.vladimirmi.photon.data.managers

import io.github.vladimirmi.photon.data.models.realm.Changeable
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.utils.Query
import io.github.vladimirmi.photon.utils.RealmOperator
import io.github.vladimirmi.photon.utils.asFlowable
import io.github.vladimirmi.photon.utils.prepareQuery
import io.reactivex.Observable
import io.realm.Realm
import io.realm.RealmObject
import io.realm.Sort
import timber.log.Timber
import java.util.concurrent.atomic.AtomicReference

/**
 * Created by Vladimir Mikhalev 04.06.2017.
 */

class RealmManager {

    fun save(realmObject: RealmObject) {
        Timber.e("save: ${realmObject::class.java.simpleName}")
        if (realmObject is Changeable && !realmObject.active) {
            remove(realmObject::class.java, realmObject.id)
            return
        }
        if (realmObject is Photocard) {
            realmObject.withId()
            realmObject.search = realmObject.title.toLowerCase()
        }
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction {
            it.insertOrUpdate(realmObject)
        }
        realm.close()
    }

    fun <T : RealmObject> getObject(clazz: Class<T>, id: String): Observable<T> {
        val query = listOf(Query("id", RealmOperator.EQUALTO, id))
        return search(clazz, query)
                .map { it.first() }
                .onErrorResumeNext(Observable.empty())
    }

    fun <T : RealmObject> search(clazz: Class<T>,
                                 query: List<Query>? = null,
                                 sortBy: String? = null,
                                 order: Sort = Sort.ASCENDING): Observable<List<T>> {

        return AtomicReference<Realm>().asFlowable {
            it.where(clazz).prepareQuery(query)
                    .let { if (sortBy != null) it.findAllSorted(sortBy, order) else it.findAll() }
        }
                .doOnSubscribe { removeAllNotActive(clazz) }
                .toObservable()
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
        Timber.e("remove not active ${clazz.simpleName}")
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

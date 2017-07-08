package io.github.vladimirmi.photon.data.managers

import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.realm.*
import timber.log.Timber
import java.lang.IllegalStateException

/**
 * Created by Vladimir Mikhalev 04.06.2017.
 */

class RealmManager {

    fun saveAsync(realmObject: RealmObject) {
        val realm = Realm.getDefaultInstance()
        realm.executeTransactionAsync(Realm.Transaction { realm.insertOrUpdate(realmObject) },
                Realm.Transaction.OnError { Timber.e("save: error ${it.message}") })
        realm.close()
    }

    fun save(realmObject: RealmObject) {
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction { realm.insertOrUpdate(realmObject) }
        realm.close()
    }

    fun <T : RealmObject> getObject(clazz: Class<T>, id: String): Observable<T> {
        val realm = Realm.getDefaultInstance()
        return RealmObjectObservable.from(realm
                .where(clazz)
                .equalTo("id", id)
                .findFirstAsync())
                .filter { it.isLoaded }
                .map { realm.copyFromRealm(it) }
                .doFinally { realm.close() }
    }


    fun <T : RealmObject> get(clazz: Class<T>, id: String): Observable<T> {
        val realm = Realm.getDefaultInstance()
        return RealmResultObservable.from(realm
                .where(clazz)
                .equalTo("id", id)
                .findAllAsync())
                .filter { it.isLoaded }
                .flatMap {
                    if (it.isNotEmpty()) {
                        Observable.just(realm.copyFromRealm(it[0]))
                    } else {
                        Observable.empty()
                    }
                }
                .doFinally { realm.close() }
    }

    fun <T : RealmObject> search(clazz: Class<T>, query: List<Query>?, sortBy: String, order: Sort): Observable<List<T>> {
        val realm = Realm.getDefaultInstance()
        var realmQuery = realm.where(clazz)

        query?.groupBy { it.fieldName }?.forEach { (_, list) ->
            realmQuery = realmQuery.beginGroup()
            list.forEachIndexed { idx, qry ->
                realmQuery = qry.applyTo(realmQuery)
                if (idx < list.size - 1) realmQuery = realmQuery.or()
            }
            realmQuery = realmQuery.endGroup()
        }

        return RealmResultObservable.from(realmQuery
                .findAllSortedAsync(sortBy, order))
                .filter { it.isLoaded }
                .map { realm.copyFromRealm(it) }
                .doFinally { realm.close() }
    }

    fun <T : RealmObject> remove(clazz: Class<T>, id: String) {
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction {
            it.where(clazz).equalTo("id", id).findFirst()?.deleteFromRealm()
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

enum class RealmOperator {CONTAINS, EQUALTO }

class Query(val fieldName: String, val operator: RealmOperator, val value: Any) {

    fun <T : RealmModel> applyTo(realmQuery: RealmQuery<T>): RealmQuery<T> {
        when (value) {
            is String -> when (operator) {
                RealmOperator.CONTAINS -> realmQuery.contains(fieldName, value)
                RealmOperator.EQUALTO -> realmQuery.equalTo(fieldName, value)
            }
            is Boolean -> when (operator) {
                RealmOperator.EQUALTO -> realmQuery.equalTo(fieldName, value)
                else -> throw IllegalStateException("Not supported operator for boolean value")
            }
        }
        return realmQuery
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        other as Query
        if (fieldName != other.fieldName) return false
        return true
    }

    override fun hashCode(): Int {
        return fieldName.hashCode()
    }

    override fun toString(): String {
        return "($fieldName $operator $value)"
    }
}

class RealmObjectObservable<T : RealmModel> private constructor(private val objekt: T)
    : ObservableOnSubscribe<T> {

    override fun subscribe(e: ObservableEmitter<T>) {
        val listener = { element: T ->
            if (!e.isDisposed) {
                e.onNext(element)
            }
        }
        RealmObject.addChangeListener(objekt, listener)
//        e.setDisposable(Disposables.fromRunnable { RealmObject.removeAllChangeListeners(objekt) })
        e.onNext(objekt)
    }

    companion object { // factory method
        fun <T : RealmModel> from(objekt: T): Observable<T> {
            return Observable.create(RealmObjectObservable(objekt))
        }
    }
}

class RealmResultObservable<T : RealmModel>(private val results: RealmResults<T>)
    : ObservableOnSubscribe<RealmResults<T>> {

    override fun subscribe(e: ObservableEmitter<RealmResults<T>>) {
        val listener = { element: RealmResults<T> ->
            if (!e.isDisposed) {
                e.onNext(element)
            }
        }
        results.addChangeListener(listener)
//        e.setDisposable(Disposables.fromRunnable { results.removeAllChangeListeners() })
        e.onNext(results)
    }

    companion object { // factory method
        fun <T : RealmModel> from(results: RealmResults<T>): Observable<RealmResults<T>> {
            return Observable.create(RealmResultObservable(results))
        }
    }
}
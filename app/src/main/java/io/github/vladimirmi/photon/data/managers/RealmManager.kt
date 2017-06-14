package io.github.vladimirmi.photon.data.managers

import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.disposables.Disposables
import io.realm.Realm
import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.RealmResults

/**
 * Created by Vladimir Mikhalev 04.06.2017.
 */


class RealmManager {


    fun save(realmObject: RealmObject) {
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction { realm.insertOrUpdate(realmObject) }
        realm.close()
    }

    fun <T : RealmObject> get(clazz: Class<T>): Observable<List<T>> {
        val realm = Realm.getDefaultInstance()
        return RealmResultObservable.from(realm
                .where(clazz)
                .findAllAsync())
                .filter { it.isLoaded }
                .map { realm.copyFromRealm(it) }
                .doAfterTerminate { realm.close() }
    }

    fun <T : RealmObject> get(clazz: Class<T>, id: String): Observable<T> {
        val realm = Realm.getDefaultInstance()
        return RealmObjectObservable.from(realm
                .where(clazz)
                .equalTo("id", id)
                .findFirstAsync())
                .filter { it.isLoaded }
                .filter { it.isValid }
                .map { realm.copyFromRealm(it) }
                .doAfterTerminate { realm.close() }
    }
}

class RealmObjectObservable<T : RealmModel> private constructor(private val objekt: T) : ObservableOnSubscribe<T> {
    override fun subscribe(e: ObservableEmitter<T>) {
        val listener = { element: T ->
            if (!e.isDisposed) {
                e.onNext(element)
            }
        }
        RealmObject.addChangeListener(objekt, listener)
        e.setDisposable(Disposables.fromRunnable { RealmObject.removeAllChangeListeners(objekt) })
        e.onNext(objekt)
    }

    companion object { // factory method
        fun <T : RealmModel> from(objekt: T): Observable<T> {
            return Observable.create(RealmObjectObservable(objekt))
        }
    }
}

class RealmResultObservable<T : RealmModel>(private val results: RealmResults<T>) : ObservableOnSubscribe<RealmResults<T>> {
    override fun subscribe(e: ObservableEmitter<RealmResults<T>>) {
        val listener = { element: RealmResults<T> ->
            if (!e.isDisposed) {
                e.onNext(element)
            }
        }
        results.addChangeListener(listener)
        e.setDisposable(Disposables.fromRunnable { results.removeAllChangeListeners() })
        e.onNext(results)
    }

    companion object {
        fun <T : RealmModel> from(results: RealmResults<T>): Observable<RealmResults<T>> {
            return Observable.create(RealmResultObservable(results))
        }
    }
}
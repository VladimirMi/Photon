package io.github.vladimirmi.photon.utils

import android.os.HandlerThread
import android.os.Process.THREAD_PRIORITY_BACKGROUND
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.realm.*


/**
 * Created by Vladimir Mikhalev 09.07.2017.
 */

enum class RealmOperator {CONTAINS, EQUALTO }

data class Query(val fieldName: String, val operator: RealmOperator, val value: Any) {

    fun <T : RealmModel> applyTo(realmQuery: RealmQuery<T>): RealmQuery<T> {
        when (value) {
            is String -> when (operator) {
                RealmOperator.CONTAINS -> realmQuery.contains(fieldName, value, Case.INSENSITIVE)
                RealmOperator.EQUALTO -> realmQuery.equalTo(fieldName, value)
            }
            is Boolean -> when (operator) {
                RealmOperator.EQUALTO -> realmQuery.equalTo(fieldName, value)
                else -> throw IllegalArgumentException("Not supported operator for boolean value")
            }
        }
        return realmQuery
    }

    override fun toString() = "($fieldName $operator $value)"
}


fun <T : RealmObject> RealmQuery<T>.prepareQuery(query: List<Query>?)
        : RealmQuery<T> {

    query?.groupBy { it.fieldName }?.forEach { (_, list) ->
        beginGroup()
        list.forEachIndexed { idx, qry ->
            qry.applyTo(this)
            if (idx < list.size - 1) or()
        }
        endGroup()
    }
    return this
}

inline fun <T : RealmObject> asFlowable(detach: Boolean, crossinline query: (Realm) -> RealmResults<T>)
        : Flowable<List<T>> {
    val handler = HandlerThread("RealmQueryThread", THREAD_PRIORITY_BACKGROUND).apply { start() }
    val scheduler = AndroidSchedulers.from(handler.looper)

    return Flowable.create<List<T>>({ emitter ->
        val realm = Realm.getDefaultInstance()
        val listener = { result: RealmResults<T> ->
            if (!emitter.isCancelled && result.isLoaded && result.isValid) {
                emitter.onNext(if (detach) realm.copyFromRealm(result) else result)
            }
        }
        val result = query(realm)
        if (!emitter.isCancelled && result.isLoaded && result.isValid) {
            emitter.onNext(if (detach) realm.copyFromRealm(result) else result)
        }

        result.addChangeListener(listener)
        emitter.setCancellable {
            result.removeChangeListener(listener)
            realm.close()
            handler.quitSafely()
        }
    }, BackpressureStrategy.LATEST)
            .subscribeOn(scheduler)
            .unsubscribeOn(scheduler)
}
package io.github.vladimirmi.photon.utils

import android.os.HandlerThread
import android.os.Process.THREAD_PRIORITY_BACKGROUND
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.realm.*
import timber.log.Timber
import java.util.concurrent.atomic.AtomicReference


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

    override fun toString(): String {
        return "($fieldName $operator $value)"
    }
}


fun <T : RealmObject> RealmQuery<T>.prepareQuery(query: List<Query>?)
        : RealmQuery<T> {

    query?.groupBy { it.fieldName }?.forEach { (_, list) ->
        Timber.e("query group $list")
        beginGroup()
        list.forEachIndexed { idx, qry ->
            qry.applyTo(this)
            if (idx < list.size - 1) or()
        }
        endGroup()
    }
    return this
}

inline fun <T : RealmObject> AtomicReference<Realm>.asFlowable(mainThread: Boolean = false,
                                                               crossinline query: (Realm) -> RealmResults<T>)
        : Flowable<List<T>> {
    var handler: HandlerThread? = null
    val scheduler = if (mainThread) {
        AndroidSchedulers.mainThread()
    } else {
        handler = HandlerThread("RealmQueryThread", THREAD_PRIORITY_BACKGROUND).apply { start() }
        AndroidSchedulers.from(handler.looper)
    }

    return Flowable.create<List<T>>({ emitter ->
        val realm = Realm.getDefaultInstance()
        set(realm)
        val listener = { result: RealmResults<T> ->
            if (!emitter.isCancelled && result.isLoaded && result.isValid) {
                emitter.onNext(result)
            }
        }
        val result = query(realm)
        if (!emitter.isCancelled && result.isLoaded && result.isValid) {
            emitter.onNext(result)
        }

        result.addChangeListener(listener)
        emitter.setCancellable {
            result.removeChangeListener(listener)
            realm.close()
            handler?.quitSafely()
        }
    }, BackpressureStrategy.LATEST)
            .subscribeOn(scheduler)
            .unsubscribeOn(scheduler)
}
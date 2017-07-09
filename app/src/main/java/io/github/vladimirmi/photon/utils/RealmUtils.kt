package io.github.vladimirmi.photon.utils

import io.reactivex.*
import io.reactivex.disposables.Disposables
import io.realm.*

/**
 * Created by Vladimir Mikhalev 09.07.2017.
 */

enum class RealmOperator {CONTAINS, EQUALTO }

class Query(val fieldName: String, val operator: RealmOperator, val value: Any) {

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

class RealmObjectFlowable<T : RealmModel> private constructor(private val objekt: T)
    : FlowableOnSubscribe<T> {

    override fun subscribe(e: FlowableEmitter<T>) {
        RealmObject.addChangeListener(objekt, RealmChangeListener {
            if (!e.isCancelled) {
                e.onNext(it)
            }
        })
        e.setDisposable(Disposables.fromRunnable { RealmObject.removeAllChangeListeners(objekt) })
        e.onNext(objekt)
    }

    companion object { // factory methods
        fun <T : RealmModel> from(objekt: T): Flowable<T> {
            return Flowable.create(RealmObjectFlowable(objekt), BackpressureStrategy.LATEST)
        }

        fun <T : RealmModel> obsFrom(objekt: T): Observable<T> {
            return from(objekt).toObservable()
        }
    }
}

class RealmResultFlowable<T : RealmModel> private constructor(private val results: RealmResults<T>)
    : FlowableOnSubscribe<RealmResults<T>> {

    override fun subscribe(e: FlowableEmitter<RealmResults<T>>) {
        results.addChangeListener(RealmChangeListener {
            if (!e.isCancelled) {
                e.onNext(it)
            }
        })
        e.setDisposable(Disposables.fromRunnable { results.removeAllChangeListeners() })
        e.onNext(results)
    }

    companion object { // factory methods
        fun <T : RealmModel> from(results: RealmResults<T>): Flowable<RealmResults<T>> {
            return Flowable.create(RealmResultFlowable(results), BackpressureStrategy.LATEST)
        }

        fun <T : RealmModel> obsFrom(results: RealmResults<T>): Observable<RealmResults<T>> {
            return from(results).toObservable()
        }
    }
}
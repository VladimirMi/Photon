package io.github.vladimirmi.photon.data.network

import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.AppConfig
import io.github.vladimirmi.photon.utils.Constants.HEADER_LAST_MODIFIED
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.functions.BiFunction
import retrofit2.Response
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Vladimir Mikhalev 04.06.2017.
 */

class LastModifiedTransformer<T>(val tag: String = "") : ObservableTransformer<Response<T>, T> {

    @Suppress("DEPRECATION")
    override fun apply(upstream: Observable<Response<T>>): ObservableSource<T> {
        return upstream.map {
            val lastModified = it.headers().get(HEADER_LAST_MODIFIED)
            val body = it.body()!!
            if (lastModified != null) {
                if (tag.isNotEmpty()) {
                    DaggerService.appComponent.dataManager().saveLastUpdate(tag, lastModified)
                }
                when (body) {
                    is User -> body.updated = Date(lastModified)
                    is Album -> body.updated = Date(lastModified)
                    is Photocard -> body.updated = Date(lastModified)
                }
            }
            return@map body
        }
    }
}

class ApiErrorTransformer<T> : ObservableTransformer<Response<T>, Response<T>> {

    override fun apply(upstream: Observable<Response<T>>): ObservableSource<Response<T>> {
        return upstream.flatMap {
            when (it.code()) {
                in 200..299 -> Observable.just(it)
                in 300..399 -> Observable.empty()
                else -> Observable.error(ApiError(it.message(), it.code(), it.errorBody()))
            }
        }
    }
}

class RetryWhenTransformer<T> : ObservableTransformer<T, T> {

    private var mThrowable: Throwable? = null

    override fun apply(upstream: Observable<T>): ObservableSource<T> {
        return upstream.retryWhen {
            it.zipWith<Int, Int>(1..5,
                    BiFunction { throwable, attempt -> mThrowable = throwable; return@BiFunction attempt })
                    .flatMap<Long> {
                        if (it == AppConfig.RETRY_REQUEST_COUNT) {
                            return@flatMap Observable.error(mThrowable)
                        } else {
                            return@flatMap Observable.just(AppConfig.RETRY_REQUEST_BASE_DELAY * Math.pow(Math.E, it.toDouble()).toLong())
                        }
                    }
                    .flatMap { Observable.timer(it, TimeUnit.MILLISECONDS) }
        }
    }
}

fun <T> Observable<T?>.notNull(): Observable<T> {
    return filter { it != null }
            .map { it!! }
}
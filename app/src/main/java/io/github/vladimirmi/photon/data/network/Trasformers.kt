package io.github.vladimirmi.photon.data.network

import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.AppConfig
import io.github.vladimirmi.photon.utils.Constants.HEADER_LAST_MODIFIED
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.functions.BiFunction
import retrofit2.Response
import java.util.concurrent.TimeUnit

/**
 * Created by Vladimir Mikhalev 04.06.2017.
 */

class RestLastModifiedTransformer<T> : ObservableTransformer<Response<T>, Response<T>> {

    override fun apply(upstream: Observable<Response<T>>): ObservableSource<Response<T>> {
        return upstream.map {
            if (it.code() == 200) {
                val lastModified = it.headers().get(HEADER_LAST_MODIFIED)
                if (lastModified != null) {
                    DaggerService.appComponent.dataManager().saveLastUpdate(lastModified)
                }
            }
            it
        }
    }
}

class RestErrorTransformer<T> : ObservableTransformer<Response<T>, T> {

    override fun apply(upstream: Observable<Response<T>>): ObservableSource<T> {
        return upstream.flatMap {
            when (it.code()) {
                200 -> Observable.just<T>(it.body())
                304 -> Observable.empty()
                else -> Observable.error(ApiError(it.message(), it.code()))
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
                            return@flatMap Observable.error<Long>(mThrowable)
                        } else {
                            return@flatMap Observable.just(AppConfig.RETRY_REQUEST_BASE_DELAY * Math.pow(Math.E, it.toDouble()).toLong())
                        }
                    }
                    .flatMap { Observable.timer(it, TimeUnit.MILLISECONDS) }
        }
    }
}
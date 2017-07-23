package io.github.vladimirmi.photon.utils

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.crashlytics.android.Crashlytics
import io.github.vladimirmi.photon.data.models.dto.PhotocardDto
import io.github.vladimirmi.photon.data.models.realm.Changeable
import io.github.vladimirmi.photon.data.network.ApiError
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import timber.log.Timber
import java.io.File
import java.net.ConnectException
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Vladimir Mikhalev 28.06.2017.
 */
fun <T> Observable<T>.observeOnMainThread(): Observable<T> = observeOn(AndroidSchedulers.mainThread())

fun <T> Observable<T>.ioToMain(): Observable<T> = subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

fun <T> Observable<T>.mainToIo(): Observable<T> = subscribeOn(AndroidSchedulers.mainThread())
        .observeOn(Schedulers.io())

fun <T> Single<T>.ioToMain(): Single<T> = subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

fun <T> Observable<T>.unit(): Observable<Unit> = map {}

fun <T> justOrEmpty(it: T): Observable<T> {
    return if (it == null) Observable.empty() else Observable.just(it)
}

fun <T> Observable<T?>.notNull(): Observable<T> = filter { it != null }
        .map { it!! }

fun <T> Observable<Response<T>>.body(): Observable<T> = map { it.body()!! }

fun <T> Observable<Response<T>>.statusCode(): Observable<Int> = map { it.code() }

fun <T> Observable<Response<T>>.parseStatusCode(): Observable<Response<T>> {
    return flatMap {
        when (it.code()) {
            in 200..299 -> Observable.just(it)
            in 300..399 -> Observable.empty()
            else -> Observable.error(ApiError(it.message(), it.code(), it.errorBody()))
        }
    }
}

@Suppress("DEPRECATION")
fun <T> Observable<Response<T>>.parseResponse(saveUpdated: ((String) -> Unit)? = null)
        : Observable<T> {
    return parseStatusCode()
            .map {
                if (saveUpdated != null) {
                    saveUpdated(Date().toString())
                }
                val body = it.body()!!
                if (body is Changeable) {
                    body.updated = Date()
                }
                return@map body
            }
}

class ErrorOnAttempt(val throwable: Throwable, val attempt: Int)

fun <T> Observable<T>.retryExp(): Observable<T> {
    return retryWhen {
        it.zipWith(1..AppConfig.RETRY_REQUEST_COUNT,
                { throwable, attempt -> ErrorOnAttempt(throwable, attempt) })
                .flatMap<Long> {
                    if (it.attempt == AppConfig.RETRY_REQUEST_COUNT) {
                        return@flatMap Observable.error(it.throwable)
                    } else {
                        val delay = AppConfig.RETRY_REQUEST_BASE_DELAY * Math.pow(Math.E, it.attempt.toDouble()).toLong()
                        return@flatMap Observable.just(delay)
                    }
                }
                .flatMap { Observable.timer(it, TimeUnit.MILLISECONDS) }
    }
}

open class ErrorObserver<T> : DisposableObserver<T>() {
    override fun onComplete() {}

    override fun onNext(it: T) {}

    override fun onError(e: Throwable) {
        if (e !is ConnectException) {
            Timber.e(e, e.localizedMessage)
            Crashlytics.logException(e)
        }
    }
}

open class ErrorSingleObserver<T> : DisposableSingleObserver<T>() {
    override fun onSuccess(t: T) {}

    override fun onError(e: Throwable) {
        if (e !is ConnectException) {
            Timber.e(e, e.localizedMessage)
            Crashlytics.logException(e)
        }
    }
}


fun PhotocardDto.downloadTo(file: File, context: Context): Single<Unit> {
    return Single.create<Unit> { e ->
        Glide.with(context)
                .load(photo)
                .asBitmap()
                .toBytes(Bitmap.CompressFormat.JPEG, 100)
                .into(object : SimpleTarget<ByteArray>() {
                    override fun onResourceReady(resource: ByteArray, glideAnimation: GlideAnimation<in ByteArray>) {
                        try {
                            file.writeBytes(resource)
                            if (!e.isDisposed) e.onSuccess(Unit)
                        } catch (exp: Exception) {
                            if (!e.isDisposed) e.onError(exp)
                        }
                    }
                })
    }
}

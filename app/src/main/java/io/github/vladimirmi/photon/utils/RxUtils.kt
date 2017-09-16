package io.github.vladimirmi.photon.utils

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.crashlytics.android.Crashlytics
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.data.network.ApiError
import io.github.vladimirmi.photon.domain.models.PhotocardDto
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableCompletableObserver
import io.reactivex.observers.DisposableObserver
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import java.net.ConnectException
import java.util.concurrent.TimeUnit

/**
 * Created by Vladimir Mikhalev 28.06.2017.
 */

fun <T> Observable<T>.ioToMain(): Observable<T> = subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

fun Completable.ioToMain(): Completable = subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

fun <T> Single<T>.ioToMain(): Single<T> = subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

fun <T> Observable<T>.unit(): Observable<Unit> = map {}

fun <T> justOrEmpty(it: T?): Observable<T> =
        if (it == null) Observable.empty() else Observable.just(it)


class ErrorOnAttempt(val throwable: Throwable, val attempt: Int)

fun <T> Observable<T>.retryExp(): Observable<T> {
    return retryWhen {
        it.zipWith(1..5, { _, attempt -> attempt as Any })
                .concatWith(it)
                .flatMap<Long> {
                    if (it is Throwable) {
                        Observable.error(it)
                    } else {
                        Observable.just(1000 * Math.pow(Math.E, (it as Int).toDouble()).toLong())
                    }
                }
                .flatMap { Observable.timer(it, TimeUnit.MILLISECONDS) }
    }
}

open class ErrorObserver<T>(private val view: BaseView<*, *>? = null) : DisposableObserver<T>() {
    override fun onComplete() {}

    override fun onNext(it: T) {}

    override fun onError(e: Throwable) {
        if (view != null && e is ApiError) view.showError(e.errorResId)
        if (e !is ConnectException) {
            Timber.e(e, e.localizedMessage)
            Crashlytics.logException(e)
        }
    }
}

open class ErrorSingleObserver<T>(private val view: BaseView<*, *>? = null) : DisposableSingleObserver<T>() {
    override fun onSuccess(it: T) {}

    override fun onError(e: Throwable) {
        if (view != null && e is ApiError) view.showError(e.errorResId)
        if (e !is ConnectException) {
            Timber.e(e, e.localizedMessage)
            Crashlytics.logException(e)
        }
    }
}

open class ErrorCompletableObserver(private val view: BaseView<*, *>? = null) : DisposableCompletableObserver() {
    override fun onComplete() {}

    override fun onError(e: Throwable) {
        if (view != null && e is ApiError) view.showError(e.errorResId)
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

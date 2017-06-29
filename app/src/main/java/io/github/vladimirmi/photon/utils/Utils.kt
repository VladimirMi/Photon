package io.github.vladimirmi.photon.utils

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

/**
 * Created by Vladimir Mikhalev 28.06.2017.
 */

fun <T> Observable<T>.ioToMain(): Observable<T> {
    return subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
}

fun <T> Observable<Response<T>>.body(): Observable<T> {
    return map { it.body()!! }
}

fun <T> Observable<Response<T>>.statusCode(): Observable<Int> {
    return map { it.code() }
}
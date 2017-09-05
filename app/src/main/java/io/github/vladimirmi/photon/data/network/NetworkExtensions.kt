package io.github.vladimirmi.photon.data.network

import io.reactivex.Maybe
import io.reactivex.Single
import retrofit2.Response
import java.util.*

/**
 * Created by Vladimir Mikhalev 04.09.2017.
 */

@Suppress("DEPRECATION")
fun <T> Single<Response<T>>.parseGetResponse(saveUpdated: ((String) -> Unit)? = null)
        : Maybe<T> {
    return flatMapMaybe {
        when (it.code()) {
            in 200..299 -> Maybe.just(it.body()!!)
            in 300..399 -> Maybe.empty()
            else -> Maybe.error(ApiError(it.message(), it.code(), it.errorBody()))
        }
    }
            .doOnSuccess { if (saveUpdated != null) saveUpdated(Date().toString()) }
}

fun <T> Single<Response<T>>.parseStatusCode(): Single<Response<T>> {
    return flatMap {
        when (it.code()) {
            in 200..299 -> Single.just(it)
            else -> Single.error(ApiError(it.message(), it.code(), it.errorBody()))
        }
    }
}

fun <T> Single<Response<T>>.body(): Single<T> = map { it.body()!! }

fun <T> Single<Response<T>>.statusCode(): Single<Int> = map { it.code() }
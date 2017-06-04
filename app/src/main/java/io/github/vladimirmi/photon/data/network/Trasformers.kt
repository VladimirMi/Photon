package io.github.vladimirmi.photon.data.network

import io.github.vladimirmi.photon.data.network.api.RestService
import io.github.vladimirmi.photon.di.DaggerService
import io.reactivex.Single
import io.reactivex.SingleSource
import io.reactivex.SingleTransformer
import retrofit2.Response

/**
 * Created by Vladimir Mikhalev 04.06.2017.
 */

class RestLastModifiedTransformer<T> : SingleTransformer<Response<T>, Response<T>> {

    override fun apply(upstream: Single<Response<T>>): SingleSource<Response<T>> {
        return upstream.map {
            if (it.code() == 200) {
                val lastModified = it.headers().get(RestService.LAST_MODIFIED)
                if (lastModified != null) {
                    DaggerService.appComponent.dataManager().saveLastUpdate(lastModified)
                }
            }
            it
        }
    }
}

class RestErrorTransformer<T> : SingleTransformer<Response<T>, T> {

    override fun apply(upstream: Single<Response<T>>): SingleSource<T> {
        return upstream.flatMap {
            if (it.code() == 200) {
                Single.just<T>(it.body())
            } else {
                Single.error(ApiError(it.message(), it.code()))
            }
        }
    }
}
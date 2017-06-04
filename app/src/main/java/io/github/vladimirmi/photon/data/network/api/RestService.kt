package io.github.vladimirmi.photon.data.network.api

import io.github.vladimirmi.photon.data.network.models.Photocard
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

interface RestService {

    companion object {
        const val IF_MODIFIED_SINCE = "If-Modified-Since"
        const val LAST_MODIFIED = "Last-Modified"
    }

    @GET("photocard/list")
    fun getPhotocards(@Header(IF_MODIFIED_SINCE) lastUpdate: String,
                      @Query("limit") limit: Int,
                      @Query("offset") offset: Int): Single<Response<List<Photocard>>>

}

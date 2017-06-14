package io.github.vladimirmi.photon.data.network.api

import io.github.vladimirmi.photon.data.models.Photocard
import io.github.vladimirmi.photon.data.models.Tag
import io.github.vladimirmi.photon.data.models.User
import io.github.vladimirmi.photon.utils.Constants.HEADER_IF_MODIFIED_SINCE
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

interface RestService {

    @GET("photocard/list")
    fun getPhotocards(@Header(HEADER_IF_MODIFIED_SINCE) lastUpdate: String,
                      @Query("limit") limit: Int,
                      @Query("offset") offset: Int)
            : Observable<Response<List<Photocard>>>

    @GET("photocard/tags")
    fun getTags(@Header(HEADER_IF_MODIFIED_SINCE) lastUpdate: String)
            : Observable<Response<List<Tag>>>

    @GET("user/{userId}")
    fun getUser(@Path("userId") id: String,
                @Header(HEADER_IF_MODIFIED_SINCE) lastUpdate: String)
            : Observable<Response<User>>

    @GET("user/{userId}/photocard/{id}")
    fun getPhotocard(@Path("id") id: String,
                     @Path("userId") userId: String,
                     @Header(HEADER_IF_MODIFIED_SINCE) lastUpdate: String)
            : Observable<Response<Photocard>>

}

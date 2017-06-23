package io.github.vladimirmi.photon.data.network.api

import io.github.vladimirmi.photon.data.models.*
import io.github.vladimirmi.photon.utils.Constants.HEADER_AUTHORIZATION
import io.github.vladimirmi.photon.utils.Constants.HEADER_IF_MODIFIED_SINCE
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.*

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

interface RestService {

    @POST("user/signIn")
    fun signIn(@Body req: SignInReq): Observable<Response<User>>

    @POST("user/signUp")
    fun signUp(@Body req: SignUpReq): Observable<Response<User>>

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

    @POST("user/{userId}/album")
    fun createAlbum(@Path("userId") userId: String,
                    @Body req: NewAlbumReq,
                    @Header(HEADER_AUTHORIZATION) token: String)
            : Observable<Response<Album>>

}

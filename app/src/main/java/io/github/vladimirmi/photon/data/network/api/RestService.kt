package io.github.vladimirmi.photon.data.network.api

import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.models.realm.Tag
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.data.models.req.*
import io.github.vladimirmi.photon.data.models.res.ImageUrlRes
import io.github.vladimirmi.photon.data.models.res.SuccessRes
import io.github.vladimirmi.photon.utils.Constants.HEADER_AUTHORIZATION
import io.github.vladimirmi.photon.utils.Constants.HEADER_IF_MODIFIED_SINCE
import io.reactivex.Observable
import okhttp3.MultipartBody
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
    fun getPhotocards(@Query("limit") limit: Int,
                      @Query("offset") offset: Int,
                      @Header(HEADER_IF_MODIFIED_SINCE) lastModified: String)
            : Observable<Response<List<Photocard>>>

    @GET("photocard/tags")
    fun getTags(@Header(HEADER_IF_MODIFIED_SINCE) lastModified: String)
            : Observable<Response<List<Tag>>>

    @GET("user/{userId}")
    fun getUser(@Path("userId") id: String,
                @Header(HEADER_IF_MODIFIED_SINCE) lastModified: String)
            : Observable<Response<User>>

    @GET("user/{userId}/photocard/{id}")
    fun getPhotocard(@Path("id") id: String,
                     @Path("userId") userId: String,
                     @Header(HEADER_IF_MODIFIED_SINCE) lastModified: String)
            : Observable<Response<Photocard>>

    @GET("user/{userId}/album/{id}")
    fun getAlbum(@Path("id") id: String,
                 @Path("userId") userId: String,
                 @Header(HEADER_IF_MODIFIED_SINCE) lastModified: String)
            : Observable<Response<Album>>

    @POST("user/{userId}/album")
    fun createAlbum(@Path("userId") userId: String,
                    @Body req: NewAlbumReq,
                    @Header(HEADER_AUTHORIZATION) token: String)
            : Observable<Response<Album>>

    @POST("user/{userId}/photocard")
    fun createPhotocard(@Path("userId") userId: String,
                        @Body req: Photocard,
                        @Header(HEADER_AUTHORIZATION) token: String)
            : Observable<Response<Photocard>>

    @Multipart
    @POST("user/{userId}/image/upload")
    fun uploadPhoto(@Path("userId") userId: String,
                    @Part bodyPart: MultipartBody.Part,
                    @Header(HEADER_AUTHORIZATION) token: String)
            : Observable<Response<ImageUrlRes>>

    @PUT("user/{userId}/album/{id}")
    fun editAlbum(@Path("userId") userId: String,
                  @Path("id") id: String,
                  @Body req: EditAlbumReq,
                  @Header(HEADER_AUTHORIZATION) token: String)
            : Observable<Response<Album>>

    @DELETE("user/{userId}/album/{id}")
    fun deleteAlbum(@Path("userId") userId: String,
                    @Path("id") id: String,
                    @Header(HEADER_AUTHORIZATION) token: String)
            : Observable<Response<Void>>

    @PUT("user/{userId}")
    fun editProfile(@Path("userId") userId: String,
                    @Body req: EditProfileReq,
                    @Header(HEADER_AUTHORIZATION) token: String)
            : Observable<Response<User>>

    @POST("photocard/{id}/view")
    fun addView(@Path("id") id: String): Observable<Response<SuccessRes>>

    @POST("user/{userId}/favorite/{id}")
    fun addToFavorite(@Path("userId") userId: String,
                      @Path("id") id: String,
                      @Header(HEADER_AUTHORIZATION) token: String)
            : Observable<Response<SuccessRes>>

    @DELETE("user/{userId}/favorite/{id}")
    fun removeFromFavorite(@Path("userId") userId: String,
                           @Path("id") id: String,
                           @Header(HEADER_AUTHORIZATION) token: String)
            : Observable<Response<Void>>

    @DELETE("user/{userId}/photocard/{id}")
    fun deletePhotocard(@Path("userId") userId: String,
                        @Path("id") id: String,
                        @Header(HEADER_AUTHORIZATION) token: String)
            : Observable<Response<Void>>
}

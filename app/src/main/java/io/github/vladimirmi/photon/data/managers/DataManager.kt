package io.github.vladimirmi.photon.data.managers

import android.content.Context
import android.net.ConnectivityManager
import com.birbit.android.jobqueue.JobManager
import io.github.vladimirmi.photon.core.App
import io.github.vladimirmi.photon.data.models.dto.Cached
import io.github.vladimirmi.photon.data.models.realm.*
import io.github.vladimirmi.photon.data.models.req.*
import io.github.vladimirmi.photon.data.models.res.ImageUrlRes
import io.github.vladimirmi.photon.data.models.res.SuccessRes
import io.github.vladimirmi.photon.data.network.api.RestService
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.*
import io.reactivex.Observable
import io.realm.RealmObject
import io.realm.Sort
import okhttp3.MultipartBody
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

@DaggerScope(App::class)
class DataManager
@Inject
constructor(private val restService: RestService,
            private val preferencesManager: PreferencesManager,
            private val realmManager: RealmManager,
            private val jobManager: JobManager,
            private val context: Context) {

    //region =============== Network ==============

    fun getPhotocardsFromNet(offset: Int, limit: Int): Observable<List<Photocard>> {
        val tag = Photocard::class.java.simpleName
        return restService.getPhotocards(limit, offset, getLastUpdate(tag))
                .parseResponse { saveLastUpdate(tag, it) }
    }

    fun getPhotocardFromNet(id: String,
                            ownerId: String,
                            lastModified: String = getLastUpdate(Photocard::class.java, id))
            : Observable<Photocard> {
        return restService.getPhotocard(id, ownerId, lastModified)
                .parseResponse()
    }

    fun getTagsFromNet(): Observable<List<Tag>> {
        val tag = Tag::class.java.simpleName
        return restService.getTags(getLastUpdate(tag))
                .parseResponse { saveLastUpdate(tag, it) }
    }

    fun getUserFromNet(id: String,
                       lastModified: String = getLastUpdate(User::class.java, id))
            : Observable<User> {
        return restService.getUser(id, lastModified)
                .parseResponse()
    }

    fun getAlbumFromNet(id: String,
                        lastModified: String = getLastUpdate(Album::class.java, id))
            : Observable<Album> {
        return restService.getAlbum(id, getProfileId(), lastModified)
                .parseResponse()
    }

    fun signIn(req: SignInReq): Observable<User> {
        return restService.signIn(req)
                .parseStatusCode()
                .body()
    }

    fun signUp(req: SignUpReq): Observable<User> {
        return restService.signUp(req)
                .parseStatusCode()
                .body()
    }

    fun createAlbum(albumNewReq: AlbumNewReq): Observable<Album> {
        return restService.createAlbum(getProfileId(), albumNewReq, getUserToken())
                .parseStatusCode()
                .body()
    }

    fun uploadPhoto(bodyPart: MultipartBody.Part): Observable<ImageUrlRes> {
        return restService.uploadPhoto(getProfileId(), bodyPart, getUserToken())
                .parseStatusCode()
                .body()
    }

    fun createPhotocard(photocard: Photocard): Observable<Photocard> {
        return restService.createPhotocard(getProfileId(), photocard, getUserToken())
                .parseStatusCode()
                .body()
    }

    fun editAlbum(req: AlbumEditReq): Observable<Album> {
        return restService.editAlbum(getProfileId(), req.id, req, getUserToken())
                .parseStatusCode()
                .body()
    }

    fun deleteAlbum(id: String): Observable<Int> {
        return restService.deleteAlbum(getProfileId(), id, getUserToken())
                .parseStatusCode()
                .statusCode()
    }

    fun editProfile(req: ProfileEditReq): Observable<User> {
        return restService.editProfile(getProfileId(), req, getUserToken())
                .parseStatusCode()
                .body()
    }

    fun addView(id: String): Observable<SuccessRes> {
        return restService.addView(id)
                .parseStatusCode()
                .body()
    }

    fun addToFavorite(id: String): Observable<SuccessRes> {
        return restService.addToFavorite(getProfileId(), id, getUserToken())
                .parseStatusCode()
                .body()
    }

    fun removeFromFavorite(id: String): Observable<Int> {
        return restService.removeFromFavorite(getProfileId(), id, getUserToken())
                .parseStatusCode()
                .statusCode()
    }

    fun deletePhotocard(id: String): Observable<Int> {
        return restService.deletePhotocard(getProfileId(), id, getUserToken())
                .parseStatusCode()
                .statusCode()
    }

    //endregion

    //region =============== DataBase ==============

    fun <T : RealmObject> saveToDB(realmObject: T) = realmManager.save(realmObject)

    fun <T : RealmObject> getListFromDb(clazz: Class<T>,
                                        sortBy: String? = null,
                                        order: Sort = Sort.ASCENDING,
                                        mainThread: Boolean = false): Observable<List<T>> =
            search(clazz, null, sortBy, order, mainThread)

    fun <T : RealmObject> getObjectFromDb(clazz: Class<T>, id: String): Observable<T> =
            realmManager.getObject(clazz, id)

    fun <T : RealmObject> getDetachedObjFromDb(clazz: Class<T>, id: String): T? =
            realmManager.getDetachedObject(clazz, id)

    fun <T : RealmObject> search(clazz: Class<T>,
                                 query: List<Query>?,
                                 sortBy: String? = null,
                                 order: Sort = Sort.ASCENDING,
                                 mainThread: Boolean = false): Observable<List<T>> =
            realmManager.search(clazz, query, sortBy, order, mainThread)

    fun <T : RealmObject> removeFromDb(clazz: Class<T>, id: String) {
        realmManager.remove(clazz, id)
    }

    fun syncDB() = RealmSynchronizer(this, jobManager).syncAll()

    inline fun <reified T : RealmObject, R : Cached> getCached(id: String): Observable<R> =
            DaggerService.appComponent.realmManager().getCached(T::class.java, id)

    private fun <T : RealmObject> getLastUpdate(clazz: Class<T>, id: String): String {
        val obj = getDetachedObjFromDb(clazz, id) as? Synchronizable
        return (obj?.updated ?: Date(0)).toString()
    }


    //endregion

    //region =============== Shared Preferences ==============

    fun saveUserId(id: String) = preferencesManager.saveUserId(id)

    fun saveUserToken(token: String) = preferencesManager.saveUserToken(token)

    fun saveUserFavAlbumId(id: String) = preferencesManager.saveFavAlbumId(id)

    fun getProfileId() = preferencesManager.getUserId()

    fun getUserFavAlbumId() = preferencesManager.getUserFavAlbumId()

    fun isUserAuth() = preferencesManager.isUserAuth()

    fun logout() = preferencesManager.clearUser()

    private fun getLastUpdate(tag: String): String = preferencesManager.getLastUpdate(tag)

    private fun saveLastUpdate(tag: String, lastModified: String) = preferencesManager.saveLastUpdate(tag, lastModified)

    private fun getUserToken() = preferencesManager.getUserToken()

    //endregion

    fun checkNetAvail() = cm.activeNetworkInfo != null && cm.activeNetworkInfo.isConnected

    fun isNetworkAvailable(): Observable<Boolean> =
            Observable.interval(0, 2, TimeUnit.SECONDS)
                    .map { checkNetAvail() }
                    .distinctUntilChanged()
                    .doOnNext { Timber.e("isNetworkAvailable $it") }

    private val cm by lazy { context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
}



package io.github.vladimirmi.photon.data.managers

import android.content.Context
import android.net.ConnectivityManager
import com.birbit.android.jobqueue.JobManager
import io.github.vladimirmi.photon.core.App
import io.github.vladimirmi.photon.data.jobs.JobsManager
import io.github.vladimirmi.photon.data.models.dto.Cached
import io.github.vladimirmi.photon.data.models.realm.*
import io.github.vladimirmi.photon.data.models.req.*
import io.github.vladimirmi.photon.data.models.res.ImageUrlRes
import io.github.vladimirmi.photon.data.models.res.SuccessRes
import io.github.vladimirmi.photon.data.network.api.RestService
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.realm.RealmObject
import io.realm.Sort
import okhttp3.MultipartBody
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

    //todo remake
    fun getPhotocardsFromNet(offset: Int, limit: Int): Observable<List<Photocard>> {
        if (!jobsManager.syncComplete) return Observable.empty()
        val tag = Photocard::class.java.simpleName
        return restService.getPhotocards(limit, offset, getLastUpdate(tag))
                .parseGetResponse { saveLastUpdate(tag, it) }
    }

    fun getPhotocardFromNet(id: String, force: Boolean = false): Observable<Photocard> {
        if (!jobsManager.syncComplete && !force) return Observable.empty()
        return restService.getPhotocard(id, "any", getLastModified(User::class.java, id, force)) // "any" because of bug in api
                .parseGetResponse()
    }

    fun getTagsFromNet(): Observable<List<Tag>> {
        if (!jobsManager.syncComplete) return Observable.empty()
        val tag = Tag::class.java.simpleName
        return restService.getTags(getLastUpdate(tag))
                .parseGetResponse { saveLastUpdate(tag, it) }
    }

    fun getUserFromNet(id: String, force: Boolean = false): Observable<User> {
        if (!jobsManager.syncComplete && !force) return Observable.empty()
        return restService.getUser(id, getLastModified(User::class.java, id, force))
                .parseGetResponse()
    }

    fun getAlbumFromNet(id: String, force: Boolean = false): Observable<Album> {
        if (!jobsManager.syncComplete) return Observable.empty()
        return restService.getAlbum(id, getProfileId(), getLastModified(User::class.java, id, force))
                .parseGetResponse()
    }

    fun signIn(req: SignInReq): Single<User> {
        return restService.signIn(req)
                .parseStatusCode()
                .body()
    }

    fun signUp(req: SignUpReq): Single<User> {
        return restService.signUp(req)
                .parseStatusCode()
                .body()
    }

    fun createAlbum(request: AlbumNewReq): Single<Album> {
        return restService.createAlbum(getProfileId(), request, getUserToken())
                .parseStatusCode()
                .body()
    }

    fun uploadPhoto(bodyPart: MultipartBody.Part): Single<ImageUrlRes> {
        return restService.uploadPhoto(getProfileId(), bodyPart, getUserToken())
                .parseStatusCode()
                .body()
    }

    fun createPhotocard(photocard: Photocard): Single<Photocard> {
        return restService.createPhotocard(getProfileId(), photocard, getUserToken())
                .parseStatusCode()
                .body()
    }

    fun editAlbum(req: AlbumEditReq): Single<Album> {
        return restService.editAlbum(getProfileId(), req.id, req, getUserToken())
                .parseStatusCode()
                .body()
    }

    fun deleteAlbum(id: String): Single<Int> {
        return restService.deleteAlbum(getProfileId(), id, getUserToken())
                .parseStatusCode()
                .statusCode()
    }

    fun editProfile(req: ProfileEditReq): Single<User> {
        return restService.editProfile(getProfileId(), req, getUserToken())
                .parseStatusCode()
                .body()
    }

    fun addView(id: String): Single<SuccessRes> {
        return restService.addView(id)
                .parseStatusCode()
                .body()
    }

    fun addToFavorite(id: String): Single<SuccessRes> {
        return restService.addToFavorite(getProfileId(), id, getUserToken())
                .parseStatusCode()
                .body()
    }

    fun removeFromFavorite(id: String): Single<Int> {
        return restService.removeFromFavorite(getProfileId(), id, getUserToken())
                .parseStatusCode()
                .statusCode()
    }

    fun deletePhotocard(id: String): Single<Int> {
        return restService.deletePhotocard(getProfileId(), id, getUserToken())
                .parseStatusCode()
                .statusCode()
    }

    private fun <T : RealmObject> getLastModified(clazz: Class<T>, id: String, force: Boolean) =
            if (force) "0" else getLastUpdate(clazz, id)

    //endregion

    //region =============== DataBase ==============

    fun <T : RealmObject> saveFromServer(realmObject: T) {
        realmManager.saveFromServer(realmObject)
    }

    fun <T : RealmObject> saveFromServer(list: List<T>) {
        realmManager.saveFromServer(list)
    }
    fun <T : RealmObject> save(realmObject: T) {
        realmManager.save(realmObject)
    }

    fun <T : RealmObject> getListFromDb(clazz: Class<T>,
                                        sortBy: String? = null,
                                        order: Sort = Sort.ASCENDING): Observable<List<T>> =
            search(clazz, null, sortBy, order)

    fun <T : RealmObject> getObjectFromDb(clazz: Class<T>,
                                          id: String,
                                          detach: Boolean = false): Observable<T> =
            realmManager.getObject(clazz, id, detach)

    fun <T : RealmObject> getDetachedObjFromDb(clazz: Class<T>, id: String): T? =
            realmManager.getDetachedObject(clazz, id)

    fun <T : RealmObject> search(clazz: Class<T>,
                                 query: List<Query>?,
                                 sortBy: String? = null,
                                 order: Sort = Sort.ASCENDING,
                                 detach: Boolean = false): Observable<List<T>> =
            realmManager.search(clazz, query, sortBy, order, detach)

    fun <T : RealmObject> removeFromDb(clazz: Class<T>, id: String) {
        realmManager.remove(clazz, id)
    }

    private val jobsManager = JobsManager(this, jobManager)

    fun syncProfile(): Completable = jobsManager.subscribe()

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

    //todo remake on receiver
    fun checkNetAvail() = cm.activeNetworkInfo != null && cm.activeNetworkInfo.isConnected

    fun isNetworkAvailable(): Observable<Boolean> =
            Observable.interval(0, 2, TimeUnit.SECONDS)
                    .map { checkNetAvail() }
                    .distinctUntilChanged()

    private val cm by lazy { context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
}



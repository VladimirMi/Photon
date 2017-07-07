package io.github.vladimirmi.photon.data.managers

import android.content.Context
import android.net.ConnectivityManager
import io.github.vladimirmi.photon.core.App
import io.github.vladimirmi.photon.data.models.*
import io.github.vladimirmi.photon.data.models.realm.*
import io.github.vladimirmi.photon.data.network.api.RestService
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.utils.body
import io.github.vladimirmi.photon.utils.parseResponse
import io.github.vladimirmi.photon.utils.parseStatusCode
import io.github.vladimirmi.photon.utils.statusCode
import io.reactivex.Observable
import io.realm.RealmObject
import io.realm.Sort
import okhttp3.MultipartBody
import timber.log.Timber
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
            private val context: Context) {

    //region =============== Network ==============

    fun getPhotocardsFromNet(limit: Int, offset: Int): Observable<List<Photocard>> {
        val tag = Photocard::class.java.simpleName
        return restService.getPhotocards(limit, offset, getLastModified(tag))
                .parseResponse { saveLastUpdate(tag, it) }
    }

    fun getPhotocardFromNet(id: String, ownerId: String, lastModified: String): Observable<Photocard> {
        return restService.getPhotocard(id, ownerId, lastModified)
                .parseResponse()
    }

    fun getTagsFromNet(): Observable<List<Tag>> {
        val tag = Tag::class.java.simpleName
        return restService.getTags(getLastModified(tag))
                .parseResponse { saveLastUpdate(tag, it) }
    }

    fun getUserFromNet(id: String, lastModified: String): Observable<User> {
        return restService.getUser(id, lastModified)
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

    fun createAlbum(newAlbumReq: NewAlbumReq): Observable<Album> {
        return restService.createAlbum(getProfileId(), newAlbumReq, getUserToken())
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

    fun editAlbum(req: EditAlbumReq): Observable<Album> {
        return restService.editAlbum(getProfileId(), req.id, req, getUserToken())
                .parseStatusCode()
                .body()
    }

    fun deleteAlbum(id: String): Observable<Int> {
        return restService.deleteAlbum(getProfileId(), id, getUserToken())
                .parseStatusCode()
                .statusCode()
    }

    fun editProfile(req: EditProfileReq): Observable<User> {
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

    fun deletePhotocard(id: String): Observable<Int> {
        Timber.e("deletePhotocard: ")
        return restService.deletePhotocard(getProfileId(), id, getUserToken())
                .parseStatusCode()
                .statusCode()
    }

    //endregion

    //region =============== DataBase ==============

    fun <T : RealmObject> saveToDB(realmObject: T, async: Boolean = false) {
        if (removedNotActive(realmObject)) return
        if (async) {
            realmManager.saveAsync(realmObject)
        } else {
            realmManager.save(realmObject)
        }
    }

    fun <T : RealmObject> getListFromDb(clazz: Class<T>,
                                        sortBy: String,
                                        order: Sort = Sort.ASCENDING): Observable<List<T>> {
        return search(clazz, null, sortBy, order)
    }

    fun <T : RealmObject> getObjectFromDb(clazz: Class<T>, id: String): Observable<T> {
        return realmManager.get(clazz, id)
                .flatMap { if (removedNotActive(it)) Observable.empty<T>() else Observable.just(it) }
    }

    fun <T : RealmObject> getSingleObjFromDb(java: Class<T>, id: String): T? {
        return realmManager.getSingle(java, id)
    }

    fun <T : RealmObject> search(clazz: Class<T>,
                                 query: List<Query>?,
                                 sortBy: String,
                                 order: Sort = Sort.ASCENDING): Observable<List<T>> {
        Timber.e("search: ")
        return realmManager.search(clazz, query, sortBy, order)
                .map { list ->
                    if (list.isNotEmpty() && list.first() is Changeable) {
                        val cleanList = list.toMutableList()
                        cleanList.removeAll { removedNotActive(it) }
                        cleanList
                    } else list
                }
    }

    private fun <T : RealmObject> removedNotActive(realmObject: T): Boolean {
        if (realmObject is Changeable && !realmObject.active) {
            removeFromDb(realmObject::class.java, realmObject.id)
            return true
        }
        return false
    }

    fun <T : RealmObject> removeFromDb(clazz: Class<T>, id: String) {
        realmManager.remove(clazz, id)
    }

    //endregion

    //region =============== Shared Preferences ==============

    private fun getLastModified(tag: String): String = preferencesManager.getLastUpdate(tag)

    fun saveLastUpdate(tag: String, lastModified: String) = preferencesManager.saveLastUpdate(tag, lastModified)

    fun saveUserId(id: String) = preferencesManager.saveUserId(id)

    fun saveUserToken(token: String) = preferencesManager.saveUserToken(token)

    fun getProfileId() = preferencesManager.getUserId()

    fun getUserToken() = preferencesManager.getUserToken()

    fun isUserAuth() = preferencesManager.isUserAuth()

    fun logout() = preferencesManager.clearUser()

    //endregion

    fun isNetworkAvailable(): Observable<Boolean> {
        return Observable.interval(0, 3000, TimeUnit.MILLISECONDS)
                .flatMap<Boolean> { _ ->
                    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    Observable.just(cm.activeNetworkInfo != null && cm.activeNetworkInfo.isConnectedOrConnecting)
                }
                .distinctUntilChanged()
    }
}



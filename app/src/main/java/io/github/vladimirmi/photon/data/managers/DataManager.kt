package io.github.vladimirmi.photon.data.managers

import android.content.Context
import android.net.ConnectivityManager
import io.github.vladimirmi.photon.core.App
import io.github.vladimirmi.photon.data.models.*
import io.github.vladimirmi.photon.data.network.RestErrorTransformer
import io.github.vladimirmi.photon.data.network.RestLastModifiedTransformer
import io.github.vladimirmi.photon.data.network.api.RestService
import io.github.vladimirmi.photon.di.DaggerScope
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
            private val context: Context) {

    //region =============== Network ==============

    fun getPhotocardsFromNet(limit: Int, offset: Int): Observable<List<Photocard>> {
        return restService.getPhotocards(getLastUpdate(Photocard::class.java.name), limit, offset)
                .compose(RestLastModifiedTransformer(Photocard::class.java.name))
                .compose(RestErrorTransformer())
    }

    fun getPhotocardFromNet(id: String, ownerId: String): Observable<Photocard> {
        //todo смысл в last-modified?
        return restService.getPhotocard(id, ownerId, Date(0).toString())
                .compose(RestErrorTransformer())
    }

    fun getTagsFromNet(): Observable<List<Tag>> {
        return restService.getTags(getLastUpdate(Tag::class.java.name))
                .compose(RestLastModifiedTransformer(Tag::class.java.name))
                .compose(RestErrorTransformer())
    }

    fun getUserFromNet(id: String): Observable<User> {
        //todo смысл в last-modified?
        return restService.getUser(id, Date(0).toString())
                .compose(RestErrorTransformer())
    }

    fun signIn(req: SignInReq): Observable<User> {
        return restService.signIn(req)
                .compose(RestErrorTransformer())
    }

    fun signUp(req: SignUpReq): Observable<User> {
        return restService.signUp(req)
                .compose(RestErrorTransformer())
    }

    fun createAlbum(newAlbumReq: NewAlbumReq): Observable<Album> {
        return restService.createAlbum(getProfileId(), newAlbumReq, getUserToken())
                .compose(RestErrorTransformer())
    }

    fun uploadPhoto(bodyPart: MultipartBody.Part): Observable<ImageUrlRes> {
        return restService.uploadPhoto(getProfileId(), bodyPart, getUserToken())
                .compose(RestErrorTransformer())
    }

    fun createPhotocard(photocard: Photocard): Observable<Photocard> {
        return restService.createPhotocard(getProfileId(), photocard, getUserToken())
                .compose(RestErrorTransformer())
    }

    //endregion

    //region =============== DataBase ==============

    fun saveToDB(realmObject: RealmObject, async: Boolean = false) {
        Timber.e("${if (async) "async saveToDB" else "saveToDB"} $realmObject")
        if (async) {
            realmManager.saveAsync(realmObject)
        } else {
            realmManager.save(realmObject)
        }
    }

    fun <T : RealmObject> getListFromDb(clazz: Class<T>, sortBy: String, order: Sort = Sort.ASCENDING)
            : Observable<List<T>> {
        return search(clazz, null, sortBy, order)
    }

    fun <T : RealmObject> getObjectFromDb(clazz: Class<T>, id: String): Observable<T> {
        return realmManager.get(clazz, id)
    }

    fun <T : RealmObject> search(clazz: Class<T>,
                                 query: List<Query>?,
                                 sortBy: String, order: Sort = Sort.ASCENDING): Observable<List<T>> {
        return realmManager.search(clazz, query, sortBy, order)
    }

    fun <T : RealmObject> removeFromDb(clazz: Class<T>, id: String) {
        realmManager.remove(clazz, id)
    }

    //endregion

    //region =============== Shared Preferences ==============

    private fun getLastUpdate(name: String): String = preferencesManager.getLastUpdate(name)

    fun saveLastUpdate(name: String, lastModified: String) = preferencesManager.saveLastUpdate(name, lastModified)

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



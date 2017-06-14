package io.github.vladimirmi.photon.data.managers

import io.github.vladimirmi.photon.core.App
import io.github.vladimirmi.photon.data.models.Photocard
import io.github.vladimirmi.photon.data.models.Tag
import io.github.vladimirmi.photon.data.models.User
import io.github.vladimirmi.photon.data.network.RestErrorTransformer
import io.github.vladimirmi.photon.data.network.RestLastModifiedTransformer
import io.github.vladimirmi.photon.data.network.api.RestService
import io.github.vladimirmi.photon.di.DaggerScope
import io.reactivex.Observable
import io.realm.RealmObject
import java.util.*
import javax.inject.Inject

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

@DaggerScope(App::class)
class DataManager
@Inject
constructor(private val restService: RestService,
            private val preferencesManager: PreferencesManager,
            private val realmManager: RealmManager) {

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

    //endregion

    //region =============== DataBase ==============

    fun saveToDB(realmObject: RealmObject) {
        realmManager.save(realmObject)
    }

    fun <T : RealmObject> getListFromDb(clazz: Class<T>): Observable<List<T>> {
        return realmManager.get(clazz)
    }

    fun <T : RealmObject> getObjectFromDb(clazz: Class<T>, id: String): Observable<T> {
        return realmManager.get(clazz, id)
    }

    //endregion

    //region =============== Shared Preferences ==============

    private fun getLastUpdate(name: String): String {
        return preferencesManager.getLastUpdate(name)
    }

    fun saveLastUpdate(name: String, lastModified: String) {
        preferencesManager.saveLastUpdate(name, lastModified)
    }

    //endregion
}



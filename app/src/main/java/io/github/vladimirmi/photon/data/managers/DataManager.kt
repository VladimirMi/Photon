package io.github.vladimirmi.photon.data.managers

import io.github.vladimirmi.photon.core.App
import io.github.vladimirmi.photon.data.network.RestErrorTransformer
import io.github.vladimirmi.photon.data.network.RestLastModifiedTransformer
import io.github.vladimirmi.photon.data.network.api.RestService
import io.github.vladimirmi.photon.data.network.models.Photocard
import io.github.vladimirmi.photon.di.DaggerScope
import io.reactivex.Single
import io.realm.RealmObject
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

    fun getPhotocardsFromNet(): Single<List<Photocard>> {
        return restService.getPhotocards(getLastUpdate(), 10, 0)
                .compose(RestLastModifiedTransformer())
                .compose(RestErrorTransformer())
    }

    //endregion

    //region =============== DataBase ==============

    fun saveToDB(realmObject: RealmObject) {
        realmManager.saveToDB(realmObject)
    }

    //endregion

    //region =============== Shared Preferences ==============

    private fun getLastUpdate(): String {
        return preferencesManager.getLastUpdate()
    }

    fun saveLastUpdate(lastModified: String) {
        preferencesManager.saveLastUpdate(lastModified)
    }

    //endregion
}



package io.github.vladimirmi.photon.data.repository.photocard

import io.github.vladimirmi.photon.core.App
import io.github.vladimirmi.photon.data.managers.PreferencesManager
import io.github.vladimirmi.photon.data.managers.RealmManager
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.models.res.ImageUrlRes
import io.github.vladimirmi.photon.data.network.api.RestService
import io.github.vladimirmi.photon.data.network.body
import io.github.vladimirmi.photon.data.network.parseStatusCode
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.utils.Query
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.MultipartBody
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 04.09.2017.
 */

@DaggerScope(App::class)
class PhotocardJobRepository
@Inject constructor(realmManager: RealmManager,
                    private val restService: RestService,
                    private val preferencesManager: PreferencesManager)
    : PhotocardEntityRepository(realmManager) {

    override public fun getPhotocard(id: String): Photocard = super.getPhotocard(id)

    fun getPhotocardFromNet(id: String): Single<Photocard> =
            restService.getPhotocard(id, "any", "0").body()

    fun getNotSync(): Observable<List<Photocard>> = realmManager.search(Photocard::class.java,
            listOf(Query("sync", Query.Operator.EQUAL, false)), managed = false)

    fun create(photocard: Photocard): Single<Photocard> {
        return restService.createPhotocard(preferencesManager.getProfileId(),
                photocard, preferencesManager.getUserToken())
                .parseStatusCode()
                .body()
                .doOnSuccess { removeTemp(photocard.id) }
    }


    fun delete(id: String): Completable {
        return restService.deletePhotocard(preferencesManager.getProfileId(),
                id, preferencesManager.getUserToken())
                .parseStatusCode()
                .toCompletable()
                .doOnComplete { realmManager.remove(Photocard::class.java, id) }
    }

    fun uploadPhoto(bodyPart: MultipartBody.Part): Single<ImageUrlRes> {
        return restService.uploadPhoto(preferencesManager.getProfileId(),
                bodyPart, preferencesManager.getUserToken())
                .parseStatusCode()
                .body()
    }

    fun addView(id: String): Completable {
        return restService.addView(id)
                .parseStatusCode()
                .toCompletable()
    }

    fun rollbackDelete(id: String) {
        with(getPhotocard(id)) {
            active = true
            save(this)
        }
    }

    fun rollbackAddView(id: String) {
        with(getPhotocard(id)) {
            views--
            save(this)
        }
    }

    private fun removeTemp(id: String) {
        if (!id.startsWith("TEMP")) throw UnsupportedOperationException()
        realmManager.remove(Photocard::class.java, id)
    }
}
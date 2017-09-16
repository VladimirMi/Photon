package io.github.vladimirmi.photon.data.repository.profile

import io.github.vladimirmi.photon.core.App
import io.github.vladimirmi.photon.data.managers.PreferencesManager
import io.github.vladimirmi.photon.data.managers.RealmManager
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.data.models.req.ProfileEditReq
import io.github.vladimirmi.photon.data.models.res.ImageUrlRes
import io.github.vladimirmi.photon.data.network.api.RestService
import io.github.vladimirmi.photon.data.network.body
import io.github.vladimirmi.photon.data.network.parseStatusCode
import io.github.vladimirmi.photon.di.DaggerScope
import io.reactivex.Single
import okhttp3.MultipartBody
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 05.09.2017.
 */

@DaggerScope(App::class)
class ProfileJobRepository
@Inject constructor(realmManager: RealmManager,
                    private val restService: RestService,
                    private val preferencesManager: PreferencesManager)
    : ProfileEntityRepository(realmManager) {

    fun getProfile() = getUser(preferencesManager.getProfileId())

    fun getProfileFromNet(): Single<User> {
        return restService.getUser(preferencesManager.getProfileId(), "0")
                .body()
    }

    fun uploadPhoto(bodyPart: MultipartBody.Part): Single<ImageUrlRes> {
        return restService.uploadPhoto(preferencesManager.getProfileId(),
                bodyPart, preferencesManager.getUserToken())
                .parseStatusCode()
                .body()
    }

    fun editProfile(req: ProfileEditReq): Single<User> {
        return restService.editProfile(preferencesManager.getProfileId(),
                req, preferencesManager.getUserToken())
                .parseStatusCode()
                .body()
    }

    fun rollbackEdit(req: ProfileEditReq) = getProfile().edit(req)
}
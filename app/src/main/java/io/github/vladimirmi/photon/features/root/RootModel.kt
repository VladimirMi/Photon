package io.github.vladimirmi.photon.features.root

import io.github.vladimirmi.photon.data.models.req.SignInReq
import io.github.vladimirmi.photon.data.models.req.SignUpReq
import io.github.vladimirmi.photon.data.repository.profile.ProfileRepository
import io.github.vladimirmi.photon.utils.ioToMain
import io.reactivex.Completable
import java.util.concurrent.TimeUnit

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

class RootModel(private val profileRepository: ProfileRepository) : IRootModel {

    override fun isUserAuth() = profileRepository.isUserAuth()

    override fun register(req: SignUpReq): Completable {
        return profileRepository.signUp(req)
                .delay(1000, TimeUnit.MILLISECONDS)
                .ioToMain()
    }

    override fun login(req: SignInReq): Completable {
        return profileRepository.signIn(req)
                .delay(1000, TimeUnit.MILLISECONDS)
                .ioToMain()
    }

    override fun logout() = profileRepository.logout()

    override fun isNetAvail() = profileRepository.isNetAvail()

    override fun syncProfile() = profileRepository.syncProfile()
}

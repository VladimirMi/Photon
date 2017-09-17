package io.github.vladimirmi.photon.domain.interactors

import io.github.vladimirmi.photon.data.models.req.SignInReq
import io.github.vladimirmi.photon.data.models.req.SignUpReq
import io.github.vladimirmi.photon.data.repository.profile.ProfileRepository
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.presentation.root.RootActivity
import io.github.vladimirmi.photon.presentation.root.RootInteractor
import io.github.vladimirmi.photon.utils.ioToMain
import io.reactivex.Completable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

@DaggerScope(RootActivity::class)
class RootInteractorImpl
@Inject constructor(private val profileRepository: ProfileRepository) : RootInteractor {

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
}

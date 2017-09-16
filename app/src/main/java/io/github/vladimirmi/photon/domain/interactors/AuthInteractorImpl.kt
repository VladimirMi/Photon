package io.github.vladimirmi.photon.domain.interactors

import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.presentation.auth.AuthInteractor
import io.github.vladimirmi.photon.presentation.auth.AuthScreen
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 25.06.2017.
 */

@DaggerScope(AuthScreen::class)
class AuthInteractorImpl
@Inject constructor()
    : AuthInteractor
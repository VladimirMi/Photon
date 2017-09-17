package io.github.vladimirmi.photon.presentation.root

import dagger.Binds
import dagger.Module
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.domain.interactors.RootInteractorImpl

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

@Module
interface RootActivityModule {
    @Binds
    @DaggerScope(RootActivity::class)
    fun rootInteractor(rootInteractor: RootInteractorImpl): RootInteractor
}

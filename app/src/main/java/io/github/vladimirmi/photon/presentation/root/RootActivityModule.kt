package io.github.vladimirmi.photon.presentation.root

import dagger.Module
import dagger.Provides
import io.github.vladimirmi.photon.data.repository.profile.ProfileRepository
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.domain.interactors.RootInteractorImpl

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

@Module
class RootActivityModule {
    //todo binds
    @Provides
    @DaggerScope(RootActivity::class)
    fun provideIRootModel(profileRepository: ProfileRepository): RootInteractor =
            RootInteractorImpl(profileRepository)

    @Provides
    @DaggerScope(RootActivity::class)
    fun provideRootPresenter(model: RootInteractor) = RootPresenter(model)
}

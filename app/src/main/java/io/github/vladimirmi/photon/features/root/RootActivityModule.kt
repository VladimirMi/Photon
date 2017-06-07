package io.github.vladimirmi.photon.features.root

import dagger.Module
import dagger.Provides
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.di.DaggerScope

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

@Module
class RootActivityModule {

    @Provides
    @DaggerScope(RootActivity::class)
    fun provideIRootModel(dataManager: DataManager): IRootModel {
        return RootModel(dataManager)
    }

    @Provides
    @DaggerScope(RootActivity::class)
    fun provideRootPresenter(model: IRootModel): RootPresenter {
        return RootPresenter(model)
    }
}

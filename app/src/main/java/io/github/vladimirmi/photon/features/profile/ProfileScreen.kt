package io.github.vladimirmi.photon.features.profile

import dagger.Provides
import dagger.Subcomponent
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseScreen
import io.github.vladimirmi.photon.data.jobs.Jobs
import io.github.vladimirmi.photon.data.managers.Cache
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.features.root.RootActivityComponent
import io.github.vladimirmi.photon.features.root.RootPresenter

/**
 * Created by Vladimir Mikhalev 15.06.2017.
 */

class ProfileScreen : BaseScreen<RootActivityComponent>() {

    override val layoutResId = R.layout.screen_profile

    //region =============== DI ==============

    override fun createScreenComponent(parentComponent: RootActivityComponent): Component {
        return parentComponent.profileComponentBuilder()
                .module(Module())
                .build()
    }

    @dagger.Module
    class Module {

        @Provides
        @DaggerScope(ProfileScreen::class)
        fun provideProfileModel(dataManager: DataManager, cache: Cache, jobs: Jobs): IProfileModel =
                ProfileModel(dataManager, jobs, cache)

        @Provides
        @DaggerScope(ProfileScreen::class)
        fun provideProfilePresenter(model: IProfileModel,
                                    rootPresenter: RootPresenter): ProfilePresenter =
                ProfilePresenter(model, rootPresenter)
    }

    @DaggerScope(ProfileScreen::class)
    @dagger.Subcomponent(modules = arrayOf(Module::class))
    interface Component {
        @Subcomponent.Builder
        interface Builder {
            fun module(module: Module): Component.Builder
            fun build(): Component
        }

        fun inject(profileView: ProfileView)

    }

    //endregion
}


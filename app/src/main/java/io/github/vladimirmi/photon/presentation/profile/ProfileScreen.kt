package io.github.vladimirmi.photon.presentation.profile

import dagger.Binds
import dagger.Subcomponent
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseScreen
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.domain.interactors.ProfileInteractorImpl
import io.github.vladimirmi.photon.presentation.root.RootActivityComponent

/**
 * Created by Vladimir Mikhalev 15.06.2017.
 */

class ProfileScreen : BaseScreen<RootActivityComponent>() {

    override val layoutResId = R.layout.screen_profile

    //region =============== DI ==============

    override fun createScreenComponent(parentComponent: RootActivityComponent): Component =
            parentComponent.profileComponentBuilder().build()

    @dagger.Module
    interface Module {
        @Binds
        @DaggerScope(ProfileScreen::class)
        fun profileInteractor(profileInteractor: ProfileInteractorImpl): ProfileInteractor
    }

    @DaggerScope(ProfileScreen::class)
    @dagger.Subcomponent(modules = arrayOf(Module::class))
    interface Component {
        @Subcomponent.Builder
        interface Builder {
            fun build(): Component
        }

        fun inject(profileView: ProfileView)
    }

    //endregion
}


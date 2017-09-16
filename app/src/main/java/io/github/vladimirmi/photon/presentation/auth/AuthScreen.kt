package io.github.vladimirmi.photon.presentation.auth

import dagger.Binds
import dagger.Subcomponent
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseScreen
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.domain.interactors.AuthInteractorImpl
import io.github.vladimirmi.photon.presentation.root.RootActivityComponent

/**
 * Created by Vladimir Mikhalev 25.06.2017.
 */

class AuthScreen : BaseScreen<RootActivityComponent>() {

    override val layoutResId = R.layout.screen_auth

    //region =============== DI ==============

    override fun createScreenComponent(parentComponent: RootActivityComponent): Component =
            parentComponent.authComponentBuilder().build()

    @dagger.Module
    interface Module {
        @Binds
        @DaggerScope(AuthScreen::class)
        fun authInteractor(interactor: AuthInteractorImpl): AuthInteractor
    }

    @DaggerScope(AuthScreen::class)
    @dagger.Subcomponent(modules = arrayOf(Module::class))
    interface Component {
        @Subcomponent.Builder
        interface Builder {
            fun build(): Component
        }

        fun inject(authView: AuthView)
    }

    //endregion

    override fun equals(other: Any?) = this === other

    override fun hashCode() = super.hashCode()
}
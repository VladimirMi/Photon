package io.github.vladimirmi.photon.presentation.splash

import dagger.Subcomponent
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseScreen
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.domain.interactors.SplashInteractorImpl
import io.github.vladimirmi.photon.presentation.root.RootActivityComponent

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

class SplashScreen : BaseScreen<RootActivityComponent>() {

    override val layoutResId: Int
        get() = R.layout.screen_splash

    //region =============== DI ==============

    override fun createScreenComponent(parentComponent: RootActivityComponent): Component =
            parentComponent.splashComponentBuilder().build()

    @dagger.Module
    interface Module {
        fun splashInteractor(splashInteractor: SplashInteractorImpl): SplashInteractor
    }

    @DaggerScope(SplashScreen::class)
    @dagger.Subcomponent(modules = arrayOf(Module::class))
    interface Component {
        @Subcomponent.Builder
        interface Builder {
            fun build(): Component
        }

        fun inject(splashView: SplashView)
    }

    //endregion

}

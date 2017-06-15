package io.github.vladimirmi.photon.features.root

import dagger.Subcomponent
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.features.main.MainScreen
import io.github.vladimirmi.photon.features.photocard.PhotocardScreen
import io.github.vladimirmi.photon.features.profile.ProfileScreen
import io.github.vladimirmi.photon.features.splash.SplashScreen

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

@DaggerScope(RootActivity::class)
@Subcomponent(modules = arrayOf(RootActivityModule::class))
interface RootActivityComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: RootActivityModule): RootActivityComponent.Builder
        fun build(): RootActivityComponent
    }

    fun splashComponentBuilder(): SplashScreen.Component.Builder
    fun mainComponentBuilder(): MainScreen.Component.Builder
    fun photocardComponentBuilder(): PhotocardScreen.Component.Builder
    fun profileComponentBuilder(): ProfileScreen.Component.Builder

    fun inject(view: RootActivity)
}

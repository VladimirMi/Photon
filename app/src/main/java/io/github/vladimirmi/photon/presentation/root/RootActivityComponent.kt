package io.github.vladimirmi.photon.presentation.root

import dagger.Subcomponent
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.presentation.album.AlbumScreen
import io.github.vladimirmi.photon.presentation.auth.AuthScreen
import io.github.vladimirmi.photon.presentation.author.AuthorScreen
import io.github.vladimirmi.photon.presentation.main.MainScreen
import io.github.vladimirmi.photon.presentation.newcard.NewCardScreen
import io.github.vladimirmi.photon.presentation.photocard.PhotocardScreen
import io.github.vladimirmi.photon.presentation.profile.ProfileScreen
import io.github.vladimirmi.photon.presentation.splash.SplashScreen

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

@DaggerScope(RootActivity::class)
@Subcomponent(modules = arrayOf(RootActivityModule::class))
interface RootActivityComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): RootActivityComponent
    }

    fun splashComponentBuilder(): SplashScreen.Component.Builder
    fun mainComponentBuilder(): MainScreen.Component.Builder
    fun photocardComponentBuilder(): PhotocardScreen.Component.Builder
    fun profileComponentBuilder(): ProfileScreen.Component.Builder
    fun albumComponentBuilder(): AlbumScreen.Component.Builder
    fun newCardComponentBuilder(): NewCardScreen.Component.Builder
    fun authorComponentBuilder(): AuthorScreen.Component.Builder
    fun authComponentBuilder(): AuthScreen.Component.Builder

    fun inject(view: RootActivity)
}

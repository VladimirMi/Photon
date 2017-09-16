package io.github.vladimirmi.photon.presentation.album

import dagger.Binds
import dagger.Subcomponent
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseScreen
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.domain.interactors.AlbumInteractorImpl
import io.github.vladimirmi.photon.presentation.root.RootActivityComponent

/**
 * Created by Vladimir Mikhalev 19.06.2017.
 */

data class AlbumScreen(val albumId: String) : BaseScreen<RootActivityComponent>() {

    override val layoutResId = R.layout.screen_album
    override val scopeName = super.scopeName + albumId

    //region =============== DI ==============

    override fun createScreenComponent(parentComponent: RootActivityComponent): Component =
            parentComponent.albumComponentBuilder().build()

    @dagger.Module
    interface Module {
        @Binds
        @DaggerScope(AlbumScreen::class)
        fun albumInteractor(interactor: AlbumInteractorImpl): AlbumInteractor
    }

    @DaggerScope(AlbumScreen::class)
    @dagger.Subcomponent(modules = arrayOf(Module::class))
    interface Component {
        @Subcomponent.Builder
        interface Builder {
            fun build(): Component
        }

        fun inject(albumView: AlbumView)
    }

    //endregion
}
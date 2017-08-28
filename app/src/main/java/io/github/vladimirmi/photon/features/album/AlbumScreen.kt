package io.github.vladimirmi.photon.features.album

import dagger.Provides
import dagger.Subcomponent
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseScreen
import io.github.vladimirmi.photon.data.jobs.queue.Jobs
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.features.root.RootActivityComponent
import io.github.vladimirmi.photon.features.root.RootPresenter

/**
 * Created by Vladimir Mikhalev 19.06.2017.
 */

data class AlbumScreen(val albumId: String) : BaseScreen<RootActivityComponent>() {

    override val layoutResId = R.layout.screen_album
    override val scopeName = super.scopeName + albumId

    //region =============== DI ==============

    override fun createScreenComponent(parentComponent: RootActivityComponent): Component {
        return parentComponent.albumComponentBuilder()
                .module(Module())
                .build()
    }

    @dagger.Module
    class Module {
        @Provides
        @DaggerScope(AlbumScreen::class)
        fun provideAlbumModel(dataManager: DataManager, jobs: Jobs): IAlbumModel =
                AlbumModel(dataManager, jobs)

        @Provides
        @DaggerScope(AlbumScreen::class)
        fun provideAlbumPresenter(model: IAlbumModel, rootPresenter: RootPresenter): AlbumPresenter =
                AlbumPresenter(model, rootPresenter)
    }

    @DaggerScope(AlbumScreen::class)
    @dagger.Subcomponent(modules = arrayOf(Module::class))
    interface Component {
        @Subcomponent.Builder
        interface Builder {
            fun module(module: Module): Component.Builder
            fun build(): Component
        }

        fun inject(albumView: AlbumView)
    }

    //endregion
}
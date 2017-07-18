package io.github.vladimirmi.photon.features.album

import com.birbit.android.jobqueue.JobManager
import dagger.Provides
import dagger.Subcomponent
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseScreen
import io.github.vladimirmi.photon.data.managers.Cache
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.features.root.RootActivityComponent
import io.github.vladimirmi.photon.features.root.RootPresenter

/**
 * Created by Vladimir Mikhalev 19.06.2017.
 */

data class AlbumScreen(val album: AlbumDto) : BaseScreen<RootActivityComponent>() {

    override val layoutResId = R.layout.screen_album
    override val scopeName = super.scopeName + album.id

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
        fun provideAlbumModel(dataManager: DataManager, jobManager: JobManager, cache: Cache): IAlbumModel {
            return AlbumModel(dataManager, jobManager, cache)
        }

        @Provides
        @DaggerScope(AlbumScreen::class)
        fun provideAlbumPresenter(model: IAlbumModel, rootPresenter: RootPresenter): AlbumPresenter {
            return AlbumPresenter(model, rootPresenter)
        }
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
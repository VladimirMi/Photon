package io.github.vladimirmi.photon.features.photocard

import dagger.Provides
import dagger.Subcomponent
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseScreen
import io.github.vladimirmi.photon.data.managers.Cache
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.features.root.RootActivityComponent
import io.github.vladimirmi.photon.features.root.RootPresenter

/**
 * Created by Vladimir Mikhalev 14.06.2017.
 */

data class PhotocardScreen(val photocardId: String, val ownerId: String) : BaseScreen<RootActivityComponent>() {
    override val layoutResId = R.layout.screen_photocard
    override val scopeName = super.scopeName + photocardId

    //region =============== DI ==============

    override fun createScreenComponent(parentComponent: RootActivityComponent): Component {
        return parentComponent.photocardComponentBuilder()
                .module(Module())
                .build()
    }

    @dagger.Module
    class Module {
        @Provides
        @DaggerScope(PhotocardScreen::class)
        fun providePhotocardModel(dataManager: DataManager, cache: Cache): IPhotocardModel {
            return PhotocardModel(dataManager, cache)
        }

        @Provides
        @DaggerScope(PhotocardScreen::class)
        fun providePhotocardPresenter(model: IPhotocardModel, rootPresenter: RootPresenter): PhotocardPresenter {
            return PhotocardPresenter(model, rootPresenter)
        }
    }

    @DaggerScope(PhotocardScreen::class)
    @dagger.Subcomponent(modules = arrayOf(Module::class))
    interface Component {
        @Subcomponent.Builder
        interface Builder {
            fun module(module: Module): Component.Builder
            fun build(): Component
        }

        fun inject(photocardView: PhotocardView)
    }

    //endregion
}
package io.github.vladimirmi.photon.features.photocard

import dagger.Provides
import dagger.Subcomponent
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseScreen
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.Photocard
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.features.root.RootActivityComponent
import io.github.vladimirmi.photon.features.root.RootPresenter

/**
 * Created by Vladimir Mikhalev 14.06.2017.
 */

class PhotocardScreen(val photocard: Photocard) : BaseScreen<RootActivityComponent>() {
    override val layoutResId = R.layout.screen_photocard

    //region =============== DI ==============

    override fun createScreenComponent(parentComponent: RootActivityComponent): Any {
        return parentComponent.photocardComponentBuilder()
                .module(Module())
                .build()
    }

    @dagger.Module
    class Module {
        @Provides
        @DaggerScope(PhotocardScreen::class)
        fun providePhotocardModel(dataManager: DataManager): IPhotocardModel {
            return PhotocardModel(dataManager)
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
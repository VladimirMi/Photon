package io.github.vladimirmi.photon.features.author

import dagger.Provides
import dagger.Subcomponent
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseScreen
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.features.root.RootActivityComponent
import io.github.vladimirmi.photon.features.root.RootPresenter

/**
 * Created by Vladimir Mikhalev 22.06.2017.
 */

class AuthorScreen(val userId: String) : BaseScreen<RootActivityComponent>() {

    override val layoutResId = R.layout.screen_author

    //region =============== DI ==============

    override fun createScreenComponent(parentComponent: RootActivityComponent): Component {
        return parentComponent.authorComponentBuilder()
                .module(Module())
                .build()
    }

    @dagger.Module
    class Module {

        @Provides
        @DaggerScope(AuthorScreen::class)
        fun provideAuthorModel(dataManager: DataManager): IAuthorModel {
            return AuthorModel(dataManager)
        }

        @Provides
        @DaggerScope(AuthorScreen::class)
        fun provideAuthorPresenter(model: IAuthorModel, rootPresenter: RootPresenter): AuthorPresenter {
            return AuthorPresenter(model, rootPresenter)
        }
    }

    @DaggerScope(AuthorScreen::class)
    @dagger.Subcomponent(modules = arrayOf(Module::class))
    interface Component {
        @Subcomponent.Builder
        interface Builder {
            fun module(module: Module): Component.Builder
            fun build(): Component
        }

        fun inject(authorView: AuthorView)
    }

    //endregion
}
package io.github.vladimirmi.photon.features.auth

import dagger.Provides
import dagger.Subcomponent
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseScreen
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.features.root.RootActivityComponent
import io.github.vladimirmi.photon.features.root.RootPresenter

/**
 * Created by Vladimir Mikhalev 25.06.2017.
 */

class AuthScreen : BaseScreen<RootActivityComponent>() {

    override val layoutResId = R.layout.screen_auth

    //region =============== DI ==============

    override fun createScreenComponent(parentComponent: RootActivityComponent): Component {
        return parentComponent.authComponentBuilder()
                .module(Module())
                .build()
    }

    @dagger.Module
    class Module {
        @Provides
        @DaggerScope(AuthScreen::class)
        fun provideAuthModel(dataManager: DataManager): IAuthModel {
            return AuthModel(dataManager)
        }

        @Provides
        @DaggerScope(AuthScreen::class)
        fun provideAuthPresenter(model: IAuthModel, rootPresenter: RootPresenter): AuthPresenter {
            return AuthPresenter(model, rootPresenter)
        }
    }

    @DaggerScope(AuthScreen::class)
    @dagger.Subcomponent(modules = arrayOf(Module::class))
    interface Component {
        @Subcomponent.Builder
        interface Builder {
            fun module(module: Module): Component.Builder
            fun build(): Component
        }

        fun inject(authView: AuthView)
    }

    //endregion
}
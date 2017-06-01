package io.github.vladimirmi.photon.features.login

import dagger.Provides
import dagger.Subcomponent
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseScreen
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.LoginDto
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.features.root.RootActivityComponent
import io.github.vladimirmi.photon.features.root.RootPresenter

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

class LoginScreen : BaseScreen<RootActivityComponent>() {

    var loginDto: LoginDto = LoginDto()

    override val layoutResId: Int
        get() = R.layout.screen_login

    //region =============== DI ==============

    override fun createScreenComponent(parentComponent: RootActivityComponent): Any {
        return parentComponent.loginComponentBuilder()
                .module(Module())
                .build()
    }

    @dagger.Module
    inner class Module {
        @Provides
        @DaggerScope(LoginScreen::class)
        internal fun provideLoginModel(dataManager: DataManager): ILoginModel {
            return LoginModel(dataManager)
        }

        @Provides
        @DaggerScope(LoginScreen::class)
        internal fun provideLoginPresenter(model: ILoginModel, rootPresenter: RootPresenter): LoginPresenter {
            return LoginPresenter(model, rootPresenter)
        }
    }

    @DaggerScope(LoginScreen::class)
    @dagger.Subcomponent(modules = arrayOf(Module::class))
    interface Component {
        @Subcomponent.Builder
        interface Builder {
            fun module(module: Module): Component.Builder
            fun build(): Component
        }

        fun inject(loginView: LoginView)
    }


    //endregion

}

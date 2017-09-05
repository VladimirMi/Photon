package io.github.vladimirmi.photon.features.splash

import dagger.Provides
import dagger.Subcomponent
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseScreen
import io.github.vladimirmi.photon.data.repository.photocard.PhotocardRepository
import io.github.vladimirmi.photon.data.repository.profile.ProfileRepository
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.features.root.RootActivityComponent
import io.github.vladimirmi.photon.features.root.RootPresenter

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

class SplashScreen : BaseScreen<RootActivityComponent>() {

    override val layoutResId: Int
        get() = R.layout.screen_splash

    //region =============== DI ==============

    override fun createScreenComponent(parentComponent: RootActivityComponent): Any {
        return parentComponent.splashComponentBuilder()
                .module(Module())
                .build()
    }

    @dagger.Module
    class Module {
        @Provides
        @DaggerScope(SplashScreen::class)
        internal fun provideSplashModel(profileRepository: ProfileRepository,
                                        photocardRepository: PhotocardRepository): ISplashModel =
                SplashModel(profileRepository, photocardRepository)

        @Provides
        @DaggerScope(SplashScreen::class)
        internal fun provideSplashPresenter(model: ISplashModel, rootPresenter: RootPresenter): SplashPresenter =
                SplashPresenter(model, rootPresenter)
    }

    @DaggerScope(SplashScreen::class)
    @dagger.Subcomponent(modules = arrayOf(Module::class))
    interface Component {
        @Subcomponent.Builder
        interface Builder {
            fun module(module: Module): Component.Builder
            fun build(): Component
        }

        fun inject(splashView: SplashView)
    }

    //endregion

}

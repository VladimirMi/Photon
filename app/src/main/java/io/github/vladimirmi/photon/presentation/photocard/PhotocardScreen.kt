package io.github.vladimirmi.photon.presentation.photocard

import dagger.Binds
import dagger.Subcomponent
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseScreen
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.domain.interactors.PhotocardInteractorImpl
import io.github.vladimirmi.photon.presentation.root.RootActivityComponent

/**
 * Created by Vladimir Mikhalev 14.06.2017.
 */

data class PhotocardScreen(val photocardId: String, val ownerId: String) : BaseScreen<RootActivityComponent>() {
    override val layoutResId = R.layout.screen_photocard
    override val scopeName = super.scopeName + photocardId

    //region =============== DI ==============

    override fun createScreenComponent(parentComponent: RootActivityComponent): Component =
            parentComponent.photocardComponentBuilder().build()

    @dagger.Module
    interface Module {
        @Binds
        @DaggerScope(PhotocardScreen::class)
        fun photocardInteractor(photocardInteractor: PhotocardInteractorImpl): PhotocardInteractor
    }

    @DaggerScope(PhotocardScreen::class)
    @dagger.Subcomponent(modules = arrayOf(Module::class))
    interface Component {
        @Subcomponent.Builder
        interface Builder {
            fun build(): Component
        }

        fun inject(photocardView: PhotocardView)
    }

    //endregion
}
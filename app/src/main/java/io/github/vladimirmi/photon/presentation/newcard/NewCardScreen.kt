package io.github.vladimirmi.photon.presentation.newcard

import android.os.Parcelable
import android.util.SparseArray
import dagger.Binds
import dagger.Subcomponent
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseScreen
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.domain.interactors.NewCardInteractorImpl
import io.github.vladimirmi.photon.presentation.newcard.album.NewCardAlbumView
import io.github.vladimirmi.photon.presentation.newcard.info.NewCardInfoView
import io.github.vladimirmi.photon.presentation.newcard.param.NewCardParamView
import io.github.vladimirmi.photon.presentation.root.RootActivityComponent

/**
 * Created by Vladimir Mikhalev 19.06.2017.
 */

class NewCardScreen(var info: NewCardScreenInfo = NewCardScreenInfo())
    : BaseScreen<RootActivityComponent>() {
    val state = SparseArray<Parcelable>()
    override val layoutResId = R.layout.screen_newcard

    //region =============== DI ==============

    override fun createScreenComponent(parentComponent: RootActivityComponent): Component =
            parentComponent.newCardComponentBuilder().build()

    @dagger.Module
    interface Module {
        @Binds
        @DaggerScope(NewCardScreen::class)
        fun newCardInteractor(interactor: NewCardInteractorImpl): NewCardInteractor
    }

    @DaggerScope(NewCardScreen::class)
    @dagger.Subcomponent(modules = arrayOf(Module::class))
    interface Component {
        @Subcomponent.Builder
        interface Builder {
            fun build(): Component
        }

        fun inject(newCardView: NewCardView)
        fun inject(newCardInfoView: NewCardInfoView)
        fun inject(newCardParamView: NewCardParamView)
        fun inject(newCardAlbumView: NewCardAlbumView)
    }
    //endregion
}
package io.github.vladimirmi.photon.features.newcard

import android.os.Parcelable
import android.util.SparseArray
import dagger.Provides
import dagger.Subcomponent
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseScreen
import io.github.vladimirmi.photon.data.mappers.AlbumCachingMapper
import io.github.vladimirmi.photon.data.repository.photocard.PhotocardRepository
import io.github.vladimirmi.photon.data.repository.profile.ProfileRepository
import io.github.vladimirmi.photon.data.repository.recents.RecentsRepository
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.features.newcard.album.NewCardAlbumPresenter
import io.github.vladimirmi.photon.features.newcard.album.NewCardAlbumView
import io.github.vladimirmi.photon.features.newcard.info.NewCardInfoPresenter
import io.github.vladimirmi.photon.features.newcard.info.NewCardInfoView
import io.github.vladimirmi.photon.features.newcard.param.NewCardParamPresenter
import io.github.vladimirmi.photon.features.newcard.param.NewCardParamView
import io.github.vladimirmi.photon.features.root.RootActivityComponent
import io.github.vladimirmi.photon.features.root.RootPresenter

/**
 * Created by Vladimir Mikhalev 19.06.2017.
 */

class NewCardScreen(var info: NewCardScreenInfo = NewCardScreenInfo())
    : BaseScreen<RootActivityComponent>() {
    val state = SparseArray<Parcelable>()
    override val layoutResId = R.layout.screen_newcard

    //region =============== DI ==============

    override fun createScreenComponent(parentComponent: RootActivityComponent): Component {
        return parentComponent.newCardComponentBuilder()
                .module(Module())
                .build()
    }

    @dagger.Module
    class Module {
        @Provides
        @DaggerScope(NewCardScreen::class)
        fun provideNewCardModel(profileRepository: ProfileRepository,
                                photocardRepository: PhotocardRepository,
                                recentsRepository: RecentsRepository,
                                albumMapper: AlbumCachingMapper): INewCardModel =
                NewCardModel(profileRepository, photocardRepository, recentsRepository, albumMapper)

        @Provides
        @DaggerScope(NewCardScreen::class)
        fun provideNewCardPresenter(model: INewCardModel,
                                    rootPresenter: RootPresenter): NewCardPresenter =
                NewCardPresenter(model, rootPresenter)

        @Provides
        @DaggerScope(NewCardScreen::class)
        fun provideNewCardInfoPresenter(model: INewCardModel,
                                        rootPresenter: RootPresenter): NewCardInfoPresenter =
                NewCardInfoPresenter(model, rootPresenter)

        @Provides
        @DaggerScope(NewCardScreen::class)
        fun provideNewCardParamPresenter(model: INewCardModel,
                                         rootPresenter: RootPresenter): NewCardParamPresenter =
                NewCardParamPresenter(model, rootPresenter)

        @Provides
        @DaggerScope(NewCardScreen::class)
        fun provideNewCardAlbumPresenter(model: INewCardModel,
                                         rootPresenter: RootPresenter): NewCardAlbumPresenter =
                NewCardAlbumPresenter(model, rootPresenter)
    }

    @DaggerScope(NewCardScreen::class)
    @dagger.Subcomponent(modules = arrayOf(Module::class))
    interface Component {
        @Subcomponent.Builder
        interface Builder {
            fun module(module: Module): Component.Builder
            fun build(): Component
        }

        fun inject(newCardView: NewCardView)
        fun inject(newCardInfoView: NewCardInfoView)
        fun inject(newCardParamView: NewCardParamView)
        fun inject(newCardAlbumView: NewCardAlbumView)
    }
    //endregion
}
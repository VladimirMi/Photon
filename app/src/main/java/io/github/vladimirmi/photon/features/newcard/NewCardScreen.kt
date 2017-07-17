package io.github.vladimirmi.photon.features.newcard

import android.os.Parcelable
import android.util.SparseArray
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

class NewCardScreen(var album: AlbumDto? = null) : BaseScreen<RootActivityComponent>() {
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
        fun provideNewCardModel(dataManager: DataManager, jobManager: JobManager, cache: Cache): INewCardModel {
            return NewCardModel(dataManager, jobManager, cache)
        }

        @Provides
        @DaggerScope(NewCardScreen::class)
        fun provideNewCardPresenter(model: INewCardModel, rootPresenter: RootPresenter): NewCardPresenter {
            return NewCardPresenter(model, rootPresenter)
        }
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
    }
    //endregion
}
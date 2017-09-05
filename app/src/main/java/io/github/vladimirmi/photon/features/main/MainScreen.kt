package io.github.vladimirmi.photon.features.main

import android.os.Parcelable
import android.util.SparseArray
import dagger.Provides
import dagger.Subcomponent
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseScreen
import io.github.vladimirmi.photon.data.mappers.PhotocardCachingMapper
import io.github.vladimirmi.photon.data.repository.photocard.PhotocardRepository
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.features.root.RootActivityComponent
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.github.vladimirmi.photon.features.search.SearchScreen
import io.github.vladimirmi.photon.features.search.SearchView
import io.github.vladimirmi.photon.utils.Query
import java.util.*

/**
 * Developer Vladimir Mikhalev, 03.06.2017.
 */

class MainScreen(var updated: Int = 0) : BaseScreen<RootActivityComponent>() {

    val state = SparseArray<Parcelable>()
    var tagsQuery = ArrayList<Query>()
    var filtersQuery = ArrayList<Query>()
    var queryPage = SearchView.Page.TAGS

    fun clearData() {
        tagsQuery.clear()
        filtersQuery.clear()
        queryPage = SearchView.Page.TAGS
    }

    override val layoutResId = R.layout.screen_main

    //region =============== DI ==============

    override fun createScreenComponent(parentComponent: RootActivityComponent): Any {
        return parentComponent.mainComponentBuilder()
                .module(Module())
                .build()
    }

    @dagger.Module
    class Module {

        @Provides
        @DaggerScope(MainScreen::class)
        fun provideMainModel(photocardRepository: PhotocardRepository,
                             photocardMapper: PhotocardCachingMapper): IMainModel =
                MainModel(photocardRepository, photocardMapper)

        @Provides
        @DaggerScope(MainScreen::class)
        fun provideMainPresenter(model: IMainModel, rootPresenter: RootPresenter): MainPresenter =
                MainPresenter(model, rootPresenter)

    }

    @DaggerScope(MainScreen::class)
    @dagger.Subcomponent(modules = arrayOf(Module::class))
    interface Component {
        @Subcomponent.Builder
        interface Builder {
            fun module(module: Module): Component.Builder
            fun build(): Component

        }

        fun inject(mainView: MainView)
        fun searchComponentBuilder(): SearchScreen.Component.Builder

    }

    //endregion
}
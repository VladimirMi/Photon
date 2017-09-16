package io.github.vladimirmi.photon.presentation.main

import android.os.Parcelable
import android.util.SparseArray
import dagger.Binds
import dagger.Subcomponent
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseScreen
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.domain.interactors.MainInteractorImpl
import io.github.vladimirmi.photon.presentation.root.RootActivityComponent
import io.github.vladimirmi.photon.presentation.search.SearchScreen
import io.github.vladimirmi.photon.presentation.search.SearchView
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

    override fun createScreenComponent(parentComponent: RootActivityComponent): Any =
            parentComponent.mainComponentBuilder().build()

    @dagger.Module
    interface Module {
        @Binds
        @DaggerScope(MainScreen::class)
        fun mainInteractor(interactor: MainInteractorImpl): MainInteractor
    }

    @DaggerScope(MainScreen::class)
    @dagger.Subcomponent(modules = arrayOf(Module::class))
    interface Component {
        @Subcomponent.Builder
        interface Builder {
            fun build(): Component
        }

        fun inject(mainView: MainView)
        fun searchComponentBuilder(): SearchScreen.Component.Builder

    }

    //endregion
}
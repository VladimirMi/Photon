package io.github.vladimirmi.photon.presentation.search

import dagger.Binds
import dagger.Subcomponent
import flow.TreeKey
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseScreen
import io.github.vladimirmi.photon.data.managers.utils.Query
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.domain.interactors.SearchInteractorImpl
import io.github.vladimirmi.photon.presentation.main.MainScreen
import io.github.vladimirmi.photon.presentation.search.filters.SearchFilterView
import io.github.vladimirmi.photon.presentation.search.tags.SearchTagView
import java.util.*

/**
 * Developer Vladimir Mikhalev, 06.06.2017.
 */

class SearchScreen : BaseScreen<MainScreen.Component>(), TreeKey {

    var tagsQuery = ArrayList<Query>()
    var filtersQuery = ArrayList<Query>()
    var queryPage = SearchView.Page.TAGS

    override val layoutResId = R.layout.screen_search
    override fun getParentKey() = MainScreen()

    //region =============== DI ==============

    override fun createScreenComponent(parentComponent: MainScreen.Component): Component =
            parentComponent.searchComponentBuilder().build()

    @dagger.Module
    interface Module {
        @Binds
        @DaggerScope(SearchScreen::class)
        fun searchInteractor(searchInteractor: SearchInteractorImpl): SearchInteractor
    }

    @DaggerScope(SearchScreen::class)
    @dagger.Subcomponent(modules = arrayOf(Module::class))
    interface Component {
        @Subcomponent.Builder
        interface Builder {
            fun build(): Component
        }

        fun inject(searchView: SearchView)
        fun inject(searchTagView: SearchTagView)
        fun inject(searchFilterView: SearchFilterView)
    }

    //endregion
}
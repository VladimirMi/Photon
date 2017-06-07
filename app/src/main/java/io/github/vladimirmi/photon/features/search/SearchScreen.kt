package io.github.vladimirmi.photon.features.search

import dagger.Provides
import dagger.Subcomponent
import flow.TreeKey
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseScreen
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.features.main.MainScreen
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.github.vladimirmi.photon.features.search.filters.SearchFilterPresenter
import io.github.vladimirmi.photon.features.search.filters.SearchFilterView
import io.github.vladimirmi.photon.features.search.tags.SearchTagPresenter
import io.github.vladimirmi.photon.features.search.tags.SearchTagView

/**
 * Developer Vladimir Mikhalev, 06.06.2017.
 */

class SearchScreen : BaseScreen<MainScreen.Component>(), TreeKey {

    override val layoutResId = R.layout.screen_search

    override fun getParentKey(): Any = MainScreen()


    //region =============== DI ==============

    override fun createScreenComponent(parentComponent: MainScreen.Component): Any {
        return parentComponent.searchComponentBuilder()
                .module(Module())
                .build()
    }

    @dagger.Module
    class Module {
        @Provides
        @DaggerScope(SearchScreen::class)
        internal fun provideSearchModel(): ISearchModel {
            return SearchModel()
        }

        @Provides
        @DaggerScope(SearchScreen::class)
        internal fun provideSearchPresenter(model: ISearchModel, rootPresenter: RootPresenter): SearchPresenter {
            return SearchPresenter(model, rootPresenter)
        }

        @Provides
        @DaggerScope(SearchScreen::class)
        internal fun provideSearchTagPresenter(model: ISearchModel, rootPresenter: RootPresenter): SearchTagPresenter {
            return SearchTagPresenter(model, rootPresenter)
        }

        @Provides
        @DaggerScope(SearchScreen::class)
        internal fun provideSearchFilterPresenter(model: ISearchModel, rootPresenter: RootPresenter): SearchFilterPresenter {
            return SearchFilterPresenter(model, rootPresenter)
        }
    }

    @DaggerScope(SearchScreen::class)
    @dagger.Subcomponent(modules = arrayOf(Module::class))
    interface Component {
        @Subcomponent.Builder
        interface Builder {
            fun module(module: Module): Component.Builder
            fun build(): Component
        }

        fun inject(searchView: SearchView)
        fun inject(searchTagView: SearchTagView)
        fun inject(searchFilterView: SearchFilterView)
    }

    //endregion
}
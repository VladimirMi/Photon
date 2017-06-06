package io.github.vladimirmi.photon.features.search

import dagger.Provides
import dagger.Subcomponent
import io.github.vladimirmi.photon.core.BaseScreen
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.features.main.MainScreen
import io.github.vladimirmi.photon.features.root.RootPresenter

/**
 * Developer Vladimir Mikhalev, 06.06.2017.
 */

class SearchScreen : BaseScreen<MainScreen.Component>() {

    override val layoutResId: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

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
    }

    //endregion
}
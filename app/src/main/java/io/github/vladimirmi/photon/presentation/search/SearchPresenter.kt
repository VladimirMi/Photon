package io.github.vladimirmi.photon.presentation.search

import flow.Flow
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.presentation.root.RootPresenter
import javax.inject.Inject

/**
 * Developer Vladimir Mikhalev, 06.06.2017.
 */

@DaggerScope(SearchScreen::class)
class SearchPresenter
@Inject constructor(model: SearchInteractor, rootPresenter: RootPresenter)
    : BasePresenter<SearchView, SearchInteractor>(model, rootPresenter) {

    override fun initToolbar() {
        rootPresenter.getNewToolbarBuilder()
                .setToolbarVisible(false)
                .setTabsEnabled(true)
                .build()
    }

    private lateinit var searchScreen: SearchScreen

    override fun initView(view: SearchView) {
        searchScreen = Flow.getKey<SearchScreen>(view)!!
        updateModel()
        view.setPage(model.queryPage)
    }

    private fun updateModel() {
        if (model.isFiltered()) return
        model.tagsQuery = searchScreen.tagsQuery
        model.filtersQuery = searchScreen.filtersQuery
        model.queryPage = searchScreen.queryPage
        model.makeQuery()
    }

    override fun onExitScope() {
        searchScreen.queryPage = model.queryPage
        searchScreen.tagsQuery = model.tagsQuery
        searchScreen.filtersQuery = model.filtersQuery
        super.onExitScope()
    }

    fun savePageType(page: SearchView.Page) {
        model.queryPage = page
    }
}


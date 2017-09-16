package io.github.vladimirmi.photon.presentation.search.filters

import flow.Flow
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.presentation.root.RootPresenter
import io.github.vladimirmi.photon.presentation.search.SearchInteractor
import io.github.vladimirmi.photon.presentation.search.SearchScreen
import io.github.vladimirmi.photon.presentation.search.SearchView
import javax.inject.Inject

@DaggerScope(SearchScreen::class)
class SearchFilterPresenter
@Inject constructor(model: SearchInteractor, rootPresenter: RootPresenter)
    : BasePresenter<SearchFilterView, SearchInteractor>(model, rootPresenter) {

    override fun initToolbar() {
        // do nothing
    }

    override fun initView(view: SearchFilterView) {
        view.restoreFilterState(model.getQuery())
        setupSubmitBtn(model.queryPage != SearchView.Page.FILTERS)
    }

    private fun setupSubmitBtn(queryChanged: Boolean) {
        view.setupSubmitBtn(queryChanged)
    }

    fun addQuery(query: Pair<String, String>) {
        model.addQuery(query)
        setupSubmitBtn(true)
    }

    fun removeQuery(query: Pair<String, String>) {
        model.removeQuery(query)
        setupSubmitBtn(true)
    }

    fun submit() {
        model.makeQuery()
        Flow.get(view).goBack()
    }

    fun submitChange() {
        model.makeQuery()
        setupSubmitBtn(true)
    }
}
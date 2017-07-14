package io.github.vladimirmi.photon.features.search.filters

import flow.Flow
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.github.vladimirmi.photon.features.search.ISearchModel
import io.github.vladimirmi.photon.features.search.SearchView

class SearchFilterPresenter(model: ISearchModel, rootPresenter: RootPresenter) :
        BasePresenter<SearchFilterView, ISearchModel>(model, rootPresenter) {

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
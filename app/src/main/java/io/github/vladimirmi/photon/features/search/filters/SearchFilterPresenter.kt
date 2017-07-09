package io.github.vladimirmi.photon.features.search.filters

import flow.Flow
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.github.vladimirmi.photon.features.search.ISearchModel
import io.github.vladimirmi.photon.features.search.SearchView

class SearchFilterPresenter(model: ISearchModel, rootPresenter: RootPresenter) :
        BasePresenter<SearchFilterView, ISearchModel>(model, rootPresenter) {

    var queryChanged = model.page != SearchView.Page.FILTERS

    override fun initToolbar() {
        // do nothing
    }

    override fun initView(view: SearchFilterView) {
        view.restoreFilterState(model.getQuery())
        view.setupSubmitBtn(queryChanged)
    }

    fun addQuery(query: Pair<String, String>) {
        model.addQuery(query)
        queryChanged = true
    }

    fun removeQuery(query: Pair<String, String>) {
        model.removeQuery(query)
        queryChanged = true
    }

    fun submit() {
        model.makeQuery()
        Flow.get(view).goBack()
    }

    fun submitChange() {
        model.makeQuery()
        queryChanged = true
        initView(view)
    }
}
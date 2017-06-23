package io.github.vladimirmi.photon.features.search.filters

import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.github.vladimirmi.photon.features.search.ISearchModel
import io.github.vladimirmi.photon.features.search.SearchPresenter

class SearchFilterPresenter(model: ISearchModel, rootPresenter: RootPresenter,
                            private val searchPresenter: SearchPresenter) :
        BasePresenter<SearchFilterView, ISearchModel>(model, rootPresenter) {

    override fun initToolbar() {
        // do nothing
    }

    override fun initView(view: SearchFilterView) {
        view.restoreFilterState(searchPresenter.getQuery())
    }

    fun addFilter(query: Pair<String, String>) {
        searchPresenter.addQuery(query)
    }

    fun removeFilter(query: Pair<String, String>) {
        //todo remove filter
    }
}
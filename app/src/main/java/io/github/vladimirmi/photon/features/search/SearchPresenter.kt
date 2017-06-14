package io.github.vladimirmi.photon.features.search

import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.features.root.RootPresenter

/**
 * Developer Vladimir Mikhalev, 06.06.2017.
 */

class SearchPresenter(model: ISearchModel, rootPresenter: RootPresenter)
    : BasePresenter<SearchView, ISearchModel>(model, rootPresenter) {

    override fun initToolbar() {
        rootPresenter.getNewRootBuilder()
                .setToolbarVisible(false)
                .setTabsEnabled(true)
                .build()
    }

    override fun initView(view: SearchView) {
        view.setPage(model.page)
    }

    fun savePageNum(page: SearchView.Page) {
        model.page = page
    }

    fun getQuery() = model.getQuery()

    fun addQuery(query: Pair<String, String>) = model.addQuery(query)

    fun makeQuery() = model.makeQuery()
}


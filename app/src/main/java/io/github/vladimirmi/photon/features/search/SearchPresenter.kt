package io.github.vladimirmi.photon.features.search

import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.features.root.RootPresenter

/**
 * Developer Vladimir Mikhalev, 06.06.2017.
 */

class SearchPresenter(model: ISearchModel, rootPresenter: RootPresenter)
    : BasePresenter<SearchView, ISearchModel>(model, rootPresenter) {

    override fun initToolbar() {
        rootPresenter.getNewToolbarBuilder()
                .setToolbarVisible(false)
                .setTabsEnabled(true)
                .build()
    }

    override fun initView(view: SearchView) {
        view.setPage(model.page)
    }

    fun savePageType(page: SearchView.Page) {
        model.page = page
    }
}


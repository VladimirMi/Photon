package io.github.vladimirmi.photon.features.search.filters

import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.github.vladimirmi.photon.features.search.ISearchModel

class SearchFilterPresenter(model: ISearchModel, rootPresenter: RootPresenter) :
        BasePresenter<SearchFilterView, ISearchModel>(model, rootPresenter) {

    override fun initView(view: SearchFilterView) {
        //todo implement me
    }

}
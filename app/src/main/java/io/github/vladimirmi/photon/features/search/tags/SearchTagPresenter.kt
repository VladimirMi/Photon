package io.github.vladimirmi.photon.features.search.tags

import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.github.vladimirmi.photon.features.search.ISearchModel
import io.github.vladimirmi.photon.features.search.SearchPresenter
import io.reactivex.disposables.Disposable

class SearchTagPresenter(model: ISearchModel, rootPresenter: RootPresenter,
                         private val searchPresenter: SearchPresenter) :
        BasePresenter<SearchTagView, ISearchModel>(model, rootPresenter) {

    override fun initToolbar() {
        // do nothing
    }

    override fun initView(view: SearchTagView) {
        compDisp.add(subscribeOnTags())
    }

    private fun subscribeOnTags(): Disposable {
        return model.getTags()
                .subscribe { view.addTags(it, searchPresenter.getQuery()) }
    }

    fun addQuery(query: Pair<String, String>) {
        searchPresenter.addQuery(query)
    }

}
package io.github.vladimirmi.photon.features.search.tags

import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.github.vladimirmi.photon.features.search.ISearchModel
import io.reactivex.disposables.Disposable

class SearchTagPresenter(model: ISearchModel, rootPresenter: RootPresenter) :
        BasePresenter<SearchTagView, ISearchModel>(model, rootPresenter) {

    override fun initView(view: SearchTagView) {
        compDisp.add(subscribeOnTags())
    }

    private fun subscribeOnTags(): Disposable {
        return model.getTags()
                .subscribe { view.addTags(it) }
    }

}
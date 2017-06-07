package io.github.vladimirmi.photon.features.search.tags

import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.github.vladimirmi.photon.features.search.ISearchModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class SearchTagPresenter(model: ISearchModel, rootPresenter: RootPresenter) :
        BasePresenter<SearchTagView, ISearchModel>(model, rootPresenter) {

    override fun initView(view: SearchTagView) {
        compDisp.add(subscribeOnTags())
    }

    private fun subscribeOnTags(): Disposable {
        return model.getTags()
                .flatMap { Observable.fromIterable(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { view.addTag(it) }
    }

}
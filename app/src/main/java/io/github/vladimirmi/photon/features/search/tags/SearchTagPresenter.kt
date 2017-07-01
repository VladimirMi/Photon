package io.github.vladimirmi.photon.features.search.tags

import flow.Flow
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.github.vladimirmi.photon.features.search.ISearchModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class SearchTagPresenter(model: ISearchModel, rootPresenter: RootPresenter) :
        BasePresenter<SearchTagView, ISearchModel>(model, rootPresenter) {

    override fun initToolbar() {
        // do nothing
    }

    override fun initView(view: SearchTagView) {
        compDisp.add(subscribeOnTags())
        compDisp.add(subscribeOnSearch())
        view.restoreFromQuery(model.getQuery())
    }

    private fun subscribeOnTags(): Disposable {
        return model.getTags()
                .subscribe { view.setTags(it, model.getQuery()) }
    }

    private fun subscribeOnSearch(): Disposable {
        return view.searchObs
                .debounce(600, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .doOnNext { addQuery(Pair("title", it.toString())) }
                .flatMap { model.search(it.toString()) }
                .subscribe { view.setRecentSearches(it) }
    }

    fun addQuery(query: Pair<String, String>) {
        model.addQuery(query)
        checkEnableSubmit()
    }

    fun removeQuery(query: Pair<String, String>) {
        model.removeQuery(query)
        checkEnableSubmit()
    }

    private fun checkEnableSubmit() {
        view.enableSubmit(model.getQuery().isNotEmpty())
    }

    fun submitSearch(search: String) {
        model.saveSearchField(search)
        model.makeQuery()
        Flow.get(view).goBack()
    }

    fun submit() {
        model.makeQuery()
    }
}

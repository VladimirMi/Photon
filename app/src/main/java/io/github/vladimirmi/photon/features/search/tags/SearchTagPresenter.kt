package io.github.vladimirmi.photon.features.search.tags

import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.github.vladimirmi.photon.features.search.ISearchModel
import io.github.vladimirmi.photon.features.search.SearchPresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class SearchTagPresenter(model: ISearchModel, rootPresenter: RootPresenter,
                         private val searchPresenter: SearchPresenter) :
        BasePresenter<SearchTagView, ISearchModel>(model, rootPresenter) {

    override fun initToolbar() {
        // do nothing
    }

    override fun initView(view: SearchTagView) {
        compDisp.add(subscribeOnTags())
        compDisp.add(subscribeOnSearch())
    }

    private fun subscribeOnTags(): Disposable {
        return model.getTags()
                .subscribe { view.addTags(it, searchPresenter.getQuery()) }
    }

    private fun subscribeOnSearch(): Disposable {
        return view.searchObs.doOnNext { view.enableSubmit(it.isNotEmpty()) }
                .debounce(600, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .doOnNext { addQuery(Pair("search", it.toString())) }
                .flatMap { model.search(it.toString()) }
                .subscribe { view.setRecentSearches(it) }
    }

    fun addQuery(query: Pair<String, String>) {
        searchPresenter.addQuery(query)
    }

    fun submitSearch(search: String) {
        if (search.isNotEmpty()) {
            model.saveSearch(search)
        }
        addQuery(Pair("search", search))
        searchPresenter.makeQuery()
    }

}

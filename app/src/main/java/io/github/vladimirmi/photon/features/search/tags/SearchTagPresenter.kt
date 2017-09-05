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
        val searchField = model.getQuery().find { it.fieldName == "searchName" }?.value as? String
        searchField?.let { view.restoreSearchField(it) }
        checkEnableSubmit()
    }

    private fun subscribeOnTags(): Disposable {
        val activeTags = model.getQuery().asSequence()
                .filter { it.fieldName == "tags.value" }.map { it.value as String }.toList()
        return model.getTags()
                .subscribe { view.setTags(it, activeTags) }
    }

    private fun subscribeOnSearch(): Disposable {
        return view.searchObs.skipInitialValue()
                .debounce(600, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .map { it.toString().toLowerCase() }
                .doOnNext {
                    model.removeQuery("searchName")
                    if (it.isNotBlank()) {
                        addQuery(Pair("searchName", it))
                    }
                }
                .flatMap { model.searchRecents(it) }
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
        if (search.isNotBlank()) model.saveSearchField(search.toLowerCase())
        model.makeQuery()
        Flow.get(view).goBack()
    }

    fun submit() {
        model.makeQuery()
    }
}

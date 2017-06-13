package io.github.vladimirmi.photon.features.search

import android.content.Context
import android.util.AttributeSet
import com.jakewharton.rxbinding2.support.v4.view.pageSelections
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.di.DaggerService
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.screen_search.view.*

/**
 * Developer Vladimir Mikhalev, 06.06.2017.
 */

class SearchView(context: Context, attrs: AttributeSet)
    : BaseView<SearchPresenter, SearchView>(context, attrs) {

    enum class Page(val position: Int) { TAGS(0), FILTERS(1) }

    private lateinit var disposable: Disposable

    override fun initDagger(context: Context) {
        DaggerService.getComponent<SearchScreen.Component>(context).inject(this)
    }

    override fun initView() {
        val tabTitles = this.resources.getStringArray(R.array.search_tabs)
        search_pager.adapter = SearchAdapter(tabTitles)
        disposable = search_pager.pageSelections().skipInitialValue()
                .subscribe {
                    presenter.savePageNum(if (it == 0) Page.TAGS else Page.FILTERS)
                }
    }

    override fun onViewDestroyed(removedByFlow: Boolean) {
        super.onViewDestroyed(removedByFlow)
        disposable.dispose()
        search_pager.adapter = null
    }

    override fun onBackPressed(): Boolean {
        presenter.makeQuery()
        return false
    }

    fun setPage(page: Page) {
        search_pager.currentItem = page.position
    }
}

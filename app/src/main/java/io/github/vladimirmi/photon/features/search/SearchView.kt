package io.github.vladimirmi.photon.features.search

import android.content.Context
import android.util.AttributeSet
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.di.DaggerService
import kotlinx.android.synthetic.main.screen_search.view.*

/**
 * Developer Vladimir Mikhalev, 06.06.2017.
 */

class SearchView(context: Context, attrs: AttributeSet)
    : BaseView<SearchPresenter, SearchView>(context, attrs) {

    enum class Page(val position: Int) { TAGS(0), FILTERS(1) }

    override fun initDagger(context: Context) {
        DaggerService.getComponent<SearchScreen.Component>(context).inject(this)
    }

    override fun initView() {
        val tabTitles = this.resources.getStringArray(R.array.search_tabs)
        search_pager.adapter = SearchAdapter(tabTitles)
    }

    override fun onViewDestroyed(removedByFlow: Boolean) {
        super.onViewDestroyed(removedByFlow)
        presenter.savePageNum(if (search_pager.currentItem == 0) Page.TAGS else Page.FILTERS)
        search_pager.adapter = null
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    fun setPage(page: Page) {
        search_pager.currentItem = page.position
    }

    fun restorePages(query: HashMap<String, MutableList<String>>) {
        val adapter: SearchAdapter = search_pager.adapter as SearchAdapter
//        adapter.restorePages(query)
    }
}

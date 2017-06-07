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

    override fun initDagger(context: Context) {
        DaggerService.getComponent<SearchScreen.Component>(context).inject(this)
    }

    override fun initView() {
        val tabTitles = this.resources.getStringArray(R.array.search_tabs)
        search_pager.adapter = SearchAdapter(tabTitles)
    }

    override fun onViewDestroyed(removedByFlow: Boolean) {
        super.onViewDestroyed(removedByFlow)
        search_pager.adapter = null
    }

    override fun onBackPressed(): Boolean {
        return false
    }
}

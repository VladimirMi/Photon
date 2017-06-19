package io.github.vladimirmi.photon.features.search.filters

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.features.search.SearchScreen
import io.github.vladimirmi.photon.ui.FilterElementView

/**
 * Created by Vladimir Mikhalev 07.06.2017.
 */

class SearchFilterView(context: Context, attrs: AttributeSet) :
        BaseView<SearchFilterPresenter, SearchFilterView>(context, attrs) {

    lateinit var filterElements: List<FilterElementView>

    override fun initDagger(context: Context) {
        DaggerService.getComponent<SearchScreen.Component>(context).inject(this)
    }

    private val filterAction: (FilterElementView) -> Unit = { select(it) }

    override fun initView() {
        filterElements = findAllFilters(this)
        filterElements.forEach { it.setAction(filterAction) }
    }

    private fun findAllFilters(view: View): List<FilterElementView> {
        val result = ArrayList<FilterElementView>()
        when (view) {
            is FilterElementView -> result.add(view)
            is ViewGroup -> {
                for (idx in 0..view.childCount - 1) {
                    result.addAll(findAllFilters(view.getChildAt(idx)))
                }
            }
        }
        return result
    }

    private fun select(filterElement: FilterElementView) {
        if (filterElement.picked) {
            presenter.addFilter(filterElement.filter)
        } else {
            presenter.removeFilter(filterElement.filter)
        }
    }

    fun restoreFilterState(filters: HashMap<String, MutableList<String>>) {
        for (view in filterElements) {
            filters[view.filter.first]?.forEach {
                if (it == view.filter.second) view.pick()
            }
        }
    }
}


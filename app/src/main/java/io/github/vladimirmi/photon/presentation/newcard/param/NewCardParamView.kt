package io.github.vladimirmi.photon.presentation.newcard.param

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import flow.Flow
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.presentation.newcard.NewCardScreen
import io.github.vladimirmi.photon.ui.FilterElementView
import java.util.*

/**
 * Created by Vladimir Mikhalev 28.07.2017.
 */

class NewCardParamView(context: Context, attrs: AttributeSet)
    : BaseView<NewCardParamPresenter, NewCardParamView>(context, attrs) {

    private val state = Flow.getKey<NewCardScreen>(context)!!.state

    private val filterAction: (FilterElementView) -> Unit = { select(it) }
    private val filterElements by lazy { findAllFilters(this) }

    override fun initDagger(context: Context) {
        DaggerService.getComponent<NewCardScreen.Component>(context).inject(this)
    }

    override fun initView() {
        initFiltersSection()
    }

    override fun onViewRestored() {
        super.onViewRestored()
        if (state.size() != 0) restoreHierarchyState(state)
    }

    override fun onViewDestroyed(removedByFlow: Boolean) {
        super.onViewDestroyed(removedByFlow)
        saveHierarchyState(state)
    }

    private fun initFiltersSection() {
        filterElements.forEach { view ->
            if (view.filter.first != "filters.nuances" && view.filter.first != "filters.dish") {
                view.radioMode = true
            }
            view.setAction(filterAction)
        }
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
}
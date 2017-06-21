package io.github.vladimirmi.photon.features.newcard

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import flow.Flow
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.ui.FilterElementView

/**
 * Created by Vladimir Mikhalev 19.06.2017.
 */

class NewCardView(context: Context, attrs: AttributeSet)
    : BaseView<NewCardPresenter, NewCardView>(context, attrs) {

    lateinit var filterElements: List<FilterElementView>
    val state = Flow.getKey<NewCardScreen>(context)!!.state

    override fun initDagger(context: Context) {
        DaggerService.getComponent<NewCardScreen.Component>(context).inject(this)
    }

    private val filterAction: (FilterElementView) -> Unit = { select(it) }

    override fun initView() {
        filterElements = findAllFilters(this)
        filterElements.forEach {
            if (it.filter.first != "nuances") it.radioMode = true
            it.setAction(filterAction)
        }
    }

    override fun onViewRestored() {
        super.onViewRestored()
        restoreHierarchyState(state)
    }

    override fun onViewDestroyed(removedByFlow: Boolean) {
        super.onViewDestroyed(removedByFlow)
        saveHierarchyState(state)
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

    private fun savePhotoCard() {

    }

    private fun select(filterElement: FilterElementView) {
        if (filterElement.picked) {
            presenter.addFilter(filterElement.filter)
        } else {
            presenter.removeFilter(filterElement.filter)
        }
    }
}


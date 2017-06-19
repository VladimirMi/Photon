package io.github.vladimirmi.photon.features.newcard

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.widget.textChanges
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.data.models.Filter
import io.github.vladimirmi.photon.data.models.Photocard
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.ui.FilterElementView
import kotlinx.android.synthetic.main.view_new_card_name.view.*
import kotlinx.android.synthetic.main.view_new_card_tags.view.*

/**
 * Created by Vladimir Mikhalev 19.06.2017.
 */

class NewCardView(context: Context, attrs: AttributeSet)
    : BaseView<NewCardPresenter, NewCardView>(context, attrs) {

    lateinit var filterElements: List<FilterElementView>

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
        presenter.nameChanges(name_field.textChanges())
        presenter.tagChanges(tag_field.textChanges())
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

    private fun setFilter(filters: Filter) {
        for (view in filterElements) {
            when (view.filter.first) {
                "dish" -> checkSelected(view, filters.dish)
                "nuances" -> checkSelected(view, filters.nuances)
                "decor" -> checkSelected(view, filters.decor)
                "temperature" -> checkSelected(view, filters.temperature)
                "light" -> checkSelected(view, filters.light)
                "lightDirection" -> checkSelected(view, filters.lightDirection)
                "lightSource" -> checkSelected(view, filters.lightSource)
            }
        }
    }

    private fun checkSelected(view: FilterElementView, value: String) {
        val values = value.split(", ")
        values.forEach {
            if (it == view.filter.second) view.pick()
        }
    }

    fun setPhotoCard(photoCard: Photocard) {
        setFilter(photoCard.filters!!)
    }
}


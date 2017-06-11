package io.github.vladimirmi.photon.features.search.filters

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.features.search.SearchScreen
import timber.log.Timber

/**
 * Created by Vladimir Mikhalev 07.06.2017.
 */

class SearchFilterView(context: Context, attrs: AttributeSet) :
        BaseView<SearchFilterPresenter, SearchFilterView>(context, attrs) {
    override fun onBackPressed(): Boolean = false

    override fun initDagger(context: Context) {
        DaggerService.getComponent<SearchScreen.Component>(context).inject(this)
    }

    override fun initView() {
        //todo implement me
        addListeners(this)
    }

    override fun onViewRestored() {
        super.onViewRestored()
    }

    private val filterAction: (FilterElementView) -> Unit = { select(it) }

    private fun addListeners(view: View) {
        when (view) {
            is FilterElementView -> {
                view.setAction(filterAction)
                return
            }
            is ViewGroup -> {
                for (idx in 1..view.childCount) {
                    addListeners(view.getChildAt(idx - 1))
                }
            }
        }
    }

    private fun select(filterElement: FilterElementView) {
        Timber.e("add query ${filterElement.query}")
    }
}


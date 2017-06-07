package io.github.vladimirmi.photon.features.search.filters

import android.content.Context
import android.util.AttributeSet
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.features.search.SearchScreen

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
    }
}


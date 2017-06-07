package io.github.vladimirmi.photon.features.search.tags

import android.content.Context
import android.util.AttributeSet
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.features.search.SearchScreen

/**
 * Developer Vladimir Mikhalev, 06.06.2017.
 */

class SearchTagView(context: Context, attrs: AttributeSet)
    : BaseView<SearchTagPresenter, SearchTagView>(context, attrs) {

    override fun onBackPressed() = false

    override fun initDagger(context: Context) {
        DaggerService.getComponent<SearchScreen.Component>(context).inject(this)
    }

    override fun initView() {
        //todo implement me
    }
}


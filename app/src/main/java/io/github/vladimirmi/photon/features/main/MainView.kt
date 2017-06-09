package io.github.vladimirmi.photon.features.main

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.util.AttributeSet
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.data.models.Photocard
import io.github.vladimirmi.photon.di.DaggerService
import kotlinx.android.synthetic.main.screen_main.view.*

/**
 * Developer Vladimir Mikhalev, 03.06.2017.
 */

class MainView(context: Context, attrs: AttributeSet) :
        BaseView<MainPresenter, MainView>(context, attrs) {

    val adapter = MainAdapter()

    override fun initDagger(context: Context) {
        DaggerService.getComponent<MainScreen.Component>(context).inject(this)
    }

    override fun initView() {
        recycler_view.layoutManager = GridLayoutManager(context, 2)
        recycler_view.adapter = adapter
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    fun setData(data: List<Photocard>) {
        adapter.updateData(data)
    }
}

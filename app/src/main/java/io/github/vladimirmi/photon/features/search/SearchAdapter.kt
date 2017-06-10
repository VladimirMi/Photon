package io.github.vladimirmi.photon.features.search

import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseView

/**
 * Developer Vladimir Mikhalev, 06.06.2017.
 */

class SearchAdapter(private val tabTitles: Array<String>) : PagerAdapter() {

    override fun isViewFromObject(view: View?, `object`: Any?): Boolean = view?.equals(`object`) ?: false

    override fun getCount(): Int = 2

    override fun getPageTitle(position: Int): CharSequence = tabTitles[position]

    override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
        container?.removeAllViews()
        if (`object` is BaseView<*, *>) `object`.onViewDestroyed(false)
    }

    override fun instantiateItem(container: ViewGroup?, position: Int): Any {
        val view: View
        if (position == 0) {
            view = LayoutInflater.from(container?.context).inflate(R.layout.view_search, container, false)
        } else {
            view = LayoutInflater.from(container?.context).inflate(R.layout.view_filters, container, false)
        }
        container?.addView(view)
        (view as BaseView<*, *>).onViewRestored()

        return view
    }
}
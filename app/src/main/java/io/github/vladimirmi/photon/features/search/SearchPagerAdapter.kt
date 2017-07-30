package io.github.vladimirmi.photon.features.search

import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseView
import java.lang.IllegalStateException

/**
 * Developer Vladimir Mikhalev, 06.06.2017.
 */

class SearchPagerAdapter(private val tabTitles: Array<String>) : PagerAdapter() {

    override fun isViewFromObject(view: View?, `object`: Any?) = view?.equals(`object`) ?: false

    override fun getCount() = 2

    override fun getPageTitle(position: Int): CharSequence = tabTitles[position]

    override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
        container?.removeView(`object` as? View)
        (`object` as? BaseView<*, *>)?.onViewDestroyed(false)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = when (position) {
            0 -> LayoutInflater.from(container.context).inflate(R.layout.view_search, container, false)
            1 -> LayoutInflater.from(container.context).inflate(R.layout.view_filters, container, false)
            else -> throw IllegalStateException("No view for given position found")
        }
        container.addView(view)
        (view as BaseView<*, *>).onViewRestored()
        return view
    }
}
package io.github.vladimirmi.photon.features.newcard

import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseView

/**
 * Created by Vladimir Mikhalev 28.07.2017.
 */


class NewCardPagerAdapter(private val count: Int) : PagerAdapter() {

    override fun isViewFromObject(view: View?, `object`: Any?) = view?.equals(`object`) ?: false

    override fun getCount() = count

    override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
        container?.removeView(`object` as? View)
        (`object` as? BaseView<*, *>)?.onViewDestroyed(false)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layout = when (Page.fromIndex(position)) {
            Page.INFO -> R.layout.view_newcard_step1
            Page.PARAMS -> R.layout.view_newcard_step2
            Page.ALBUMS -> R.layout.view_newcard_step3
        }
        val view = LayoutInflater.from(container.context).inflate(layout, container, false)
        container.addView(view)
        (view as BaseView<*, *>).onViewRestored()
        return view
    }
}

//todo remove index, use ordinal
enum class Page(val index: Int) {
    INFO(0), PARAMS(1), ALBUMS(2);

    companion object {
        private val map = Page.values().associateBy(Page::index)
        fun fromIndex(index: Int) = map[index] ?:
                throw IllegalArgumentException("No page found for given index")
    }


}
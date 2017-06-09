package io.github.vladimirmi.photon.features.search.tags

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.view.View
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.data.models.Search
import io.github.vladimirmi.photon.data.models.Tag
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.features.search.SearchScreen
import io.github.vladimirmi.photon.utils.TagView
import kotlinx.android.synthetic.main.view_tags.view.*
import timber.log.Timber

/**
 * Developer Vladimir Mikhalev, 06.06.2017.
 */

class SearchTagView(context: Context, attrs: AttributeSet)
    : BaseView<SearchTagPresenter, SearchTagView>(context, attrs) {

    val searchAdapter = RecentSearchesAdapter()

    override fun onBackPressed() = false

    override fun initDagger(context: Context) {
        DaggerService.getComponent<SearchScreen.Component>(context).inject(this)
    }

    override fun initView() {
        recent_search.layoutManager = LinearLayoutManager(context)
        recent_search.adapter = searchAdapter
        search_field.setOnClickListener { recent_search_wrapper.visibility = View.VISIBLE }
    }

    override fun onViewDestroyed(removedByFlow: Boolean) {
        super.onViewDestroyed(removedByFlow)
        recent_search.adapter = null
    }

    private val tagAction: (TagView) -> Unit = { select(it) }

    fun addTag(tag: Tag) {
        tags.addView(TagView(context, tag.tag, tagAction))
    }

    private fun select(tagView: TagView) {
        Timber.e("tagView selected with tag - ${tagView.text}")
    }

    fun setRecentSearchs(list: List<Search>) {
        searchAdapter.data = list
    }
}


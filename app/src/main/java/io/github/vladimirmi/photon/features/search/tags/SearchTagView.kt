package io.github.vladimirmi.photon.features.search.tags

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.data.models.Search
import io.github.vladimirmi.photon.data.models.Tag
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.features.search.SearchScreen
import kotlinx.android.synthetic.main.view_search.view.*
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

    fun addTags(tags: List<Tag>) {
        val tagsContainer = LayoutInflater.from(context).inflate(R.layout.view_tags, tags_wrapper, false)
        tagsContainer as ViewGroup
        tags.forEach {
            tagsContainer.addView(TagView(context, it.tag, tagAction))
        }
        tags_wrapper.addView(tagsContainer)
    }

    private fun select(tagView: TagView) {
        Timber.e("tagView selected with tag - ${tagView.text}")
    }

    fun setRecentSearchs(list: List<Search>) {
        searchAdapter.data = list
    }
}


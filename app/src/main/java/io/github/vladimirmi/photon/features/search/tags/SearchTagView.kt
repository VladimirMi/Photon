package io.github.vladimirmi.photon.features.search.tags

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.view.LayoutInflater
import com.google.android.flexbox.FlexboxLayout
import com.jakewharton.rxbinding2.widget.textChanges
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.data.models.realm.Search
import io.github.vladimirmi.photon.data.models.realm.Tag
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.features.search.SearchScreen
import io.github.vladimirmi.photon.ui.TagView
import io.github.vladimirmi.photon.utils.Query
import kotlinx.android.synthetic.main.view_search.view.*

/**
 * Developer Vladimir Mikhalev, 06.06.2017.
 */

class SearchTagView(context: Context, attrs: AttributeSet)
    : BaseView<SearchTagPresenter, SearchTagView>(context, attrs) {

    val searchObs by lazy { search_field.textChanges() }

    private val searchAction: (String) -> Unit = { search_field.setText(it) }
    private val searchAdapter = StringAdapter(searchAction)

    private val tagAction: (TagView) -> Unit = { select(it) }
    private val flexbox by lazy {
        LayoutInflater.from(context).inflate(R.layout.view_search_tags, tags_wrapper, false) as FlexboxLayout
    }

    override fun initDagger(context: Context) {
        DaggerService.getComponent<SearchScreen.Component>(context).inject(this)
    }

    override fun initView() {
        recent_search.layoutManager = LinearLayoutManager(context)
        recent_search.adapter = searchAdapter
        ic_action.setOnClickListener { presenter.submitSearch(search_field.text.toString()) }
        ic_clear_tag.setOnClickListener { clearAll() }
    }

    private fun clearAll() {
        search_field.setText("")
        (0..flexbox.childCount - 1)
                .map { flexbox.getChildAt(it) as TagView }
                .filter { it.picked }
                .forEach { it.performClick() }
        presenter.submit()
    }

    private fun select(tagView: TagView) {
        val query = Pair("tags.value", tagView.text.toString())
        if (tagView.picked) {
            presenter.addQuery(query)
        } else {
            presenter.removeQuery(query)
        }
    }

    fun setRecentSearches(list: List<Search>) {
        searchAdapter.updateData(list.map { it.value })
    }

    fun enableSubmit(enable: Boolean) {
        ic_action.setImageResource(if (enable) R.drawable.ic_action_submit else R.drawable.ic_action_back_arrow)
    }

    fun restoreFromQuery(query: List<Query>) {
        val value = query.find { it.fieldName == "search" }?.value as? String
        search_field.setText(value)
    }

    fun setTags(tags: List<Tag>, query: List<Query>) {
        val queryTags = query.filter { it.fieldName == "tags.value" }
        tags.forEach { tag ->
            val view = TagView(context, tag.value, tagAction)
            if (queryTags.find { it.value == tag.value } != null) view.pick()
            flexbox.addView(view)
        }
        tags_wrapper.addView(flexbox)
    }
}


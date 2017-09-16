package io.github.vladimirmi.photon.presentation.search.tags

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import com.jakewharton.rxbinding2.widget.textChanges
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.presentation.search.SearchScreen
import io.github.vladimirmi.photon.ui.TagView
import kotlinx.android.synthetic.main.view_search.view.*

/**
 * Developer Vladimir Mikhalev, 06.06.2017.
 */

class SearchTagView(context: Context, attrs: AttributeSet)
    : BaseView<SearchTagPresenter, SearchTagView>(context, attrs) {

    private val flexbox by lazy { flex_box }
    private val recentSearches by lazy { recent_search }
    private val searchField by lazy { search_field }
    private val actionIcon by lazy { ic_action }
    private val clearIcon by lazy { ic_clear_tag }

    val searchObs by lazy { searchField.textChanges() }

    private val searchAction: (String) -> Unit = { search_field.setText(it) }
    private val searchAdapter = StringAdapter(searchAction)

    private val tagAction: (TagView) -> Unit = { selectTag(it) }

    override fun initDagger(context: Context) {
        DaggerService.getComponent<SearchScreen.Component>(context).inject(this)
    }

    override fun initView() {
        recentSearches.layoutManager = LinearLayoutManager(context)
        recentSearches.adapter = searchAdapter
        actionIcon.setOnClickListener { presenter.submitSearch(search_field.text.toString()) }
        clearIcon.setOnClickListener { clearAll() }
    }

    private fun clearAll() {
        searchField.setText("")
        (0..flexbox.childCount - 1).asSequence()
                .map { flexbox.getChildAt(it) as TagView }
                .filter { it.picked }
                .forEach { it.performClick() }
        presenter.submit()
    }

    private fun selectTag(tagView: TagView) {
        val query = Pair("tags.value", tagView.text.toString())
        if (tagView.picked) {
            presenter.addQuery(query)
        } else {
            presenter.removeQuery(query)
        }
    }

    fun setRecentSearches(list: List<String>) {
        searchAdapter.updateData(list)
    }

    fun enableSubmit(enable: Boolean) {
        actionIcon.setImageResource(if (enable) R.drawable.ic_action_submit else R.drawable.ic_action_back_arrow)
    }

    fun restoreSearchField(value: String) {
        searchField.setText(value)
    }

    fun setTags(tags: List<String>, activeTags: List<String>) {
        tags.forEach { tag ->
            val tagView = TagView(context, tag, tagAction)
            if (activeTags.contains(tag)) tagView.pick()
            flexbox.addView(tagView)
        }
    }

    override fun onViewDestroyed(removedByFlow: Boolean) {
        super.onViewDestroyed(removedByFlow)
        recentSearches.adapter = null
    }
}


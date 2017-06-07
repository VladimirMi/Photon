package io.github.vladimirmi.photon.features.search.tags

import android.content.Context
import android.util.AttributeSet
import com.google.android.flexbox.FlexboxLayout
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.data.network.models.Tag
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

    lateinit var tagsContainer: FlexboxLayout

    override fun onBackPressed() = false

    override fun initDagger(context: Context) {
        DaggerService.getComponent<SearchScreen.Component>(context).inject(this)
    }

    override fun initView() {
        tagsContainer = tags
    }

    private val tagAction: (TagView) -> Unit = { select(it) }

    fun addTag(tag: Tag) {
        tagsContainer.addView(TagView(context, tag.tag, tagAction))
    }

    private fun select(tagView: TagView) {
        Timber.e("tagView selected with tag - ${tagView.text}")
    }
}


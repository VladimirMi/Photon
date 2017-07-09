package io.github.vladimirmi.photon.features.newcard

import android.content.Context
import android.content.Intent
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.widget.textChanges
import flow.Flow
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Tag
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.features.main.AlbumAdapter
import io.github.vladimirmi.photon.features.main.AlbumViewHolder
import io.github.vladimirmi.photon.features.search.tags.StringAdapter
import io.github.vladimirmi.photon.flow.FlowLifecycles
import io.github.vladimirmi.photon.ui.FilterElementView
import kotlinx.android.synthetic.main.screen_newcard.view.*
import kotlinx.android.synthetic.main.view_choose.view.*
import kotlinx.android.synthetic.main.view_new_card.view.*
import kotlinx.android.synthetic.main.view_new_card_name.view.*
import kotlinx.android.synthetic.main.view_new_card_tags.view.*

/**
 * Created by Vladimir Mikhalev 19.06.2017.
 */

class NewCardView(context: Context, attrs: AttributeSet)
    : BaseView<NewCardPresenter, NewCardView>(context, attrs),
        FlowLifecycles.ActivityResultListener, FlowLifecycles.PermissionRequestListener {

    private val state = Flow.getKey<NewCardScreen>(context)!!.state
    private val filterAction: (FilterElementView) -> Unit = { select(it) }
    private val filterElements by lazy { findAllFilters(this) }

    val nameObs by lazy { name_field.textChanges() }
    val tagObs by lazy { tag_field.textChanges() }


    private val tagAction: (String) -> Unit = {
        tag_field.setText(it)
        tag_field.setSelection(it.length)
    }
    private val tagsAdapter = StringAdapter(tagAction)
    private val suggestTagAdapter = StringAdapter(tagAction)

    private val albumAction: (Album) -> Unit = { presenter.setAlbumId(it.id) }
    private val albumAdapter = AlbumAdapter(albumAction)

    override fun initDagger(context: Context) {
        DaggerService.getComponent<NewCardScreen.Component>(context).inject(this)
    }

    override fun onBackPressed() = presenter.onBackPressed()

    override fun initView() {
        choose_btn.setOnClickListener { presenter.choosePhoto() }
        save.setOnClickListener { presenter.savePhotocard() }
        cancel.setOnClickListener { presenter.clearPhotocard() }
        ic_clear_name.setOnClickListener { name_field.setText("") }
        ic_clear_tag.setOnClickListener { tag_field.setText("") }
        initFiltersSection()
        initTagSection()
        album_list.layoutManager = GridLayoutManager(context, 2)
        album_list.adapter = albumAdapter
    }

    private fun initFiltersSection() {
        filterElements.forEach {
            if (it.filter.first != "filters.nuances") it.radioMode = true
            it.setAction(filterAction)
        }
    }

    private fun initTagSection() {
        tag_list.layoutManager = LinearLayoutManager(context)
        tag_list.adapter = tagsAdapter

        ic_action.setOnClickListener {
            val tag = tag_field.text.toString()
            if (tag.isNotEmpty()) {
                presenter.saveTag(tag)
                tag_field.setText("")
            }
        }

        suggestion_tag_list.layoutManager = LinearLayoutManager(context)
        suggestion_tag_list.adapter = suggestTagAdapter
    }

    override fun onViewRestored() {
        super.onViewRestored()
        if (state.size() != 0) restoreHierarchyState(state)
    }

    override fun onViewDestroyed(removedByFlow: Boolean) {
        super.onViewDestroyed(removedByFlow)
        if (removedByFlow) state.clear() else saveHierarchyState(state)
    }

    private fun findAllFilters(view: View): List<FilterElementView> {
        val result = ArrayList<FilterElementView>()
        when (view) {
            is FilterElementView -> result.add(view)
            is ViewGroup -> {
                for (idx in 0..view.childCount - 1) {
                    result.addAll(findAllFilters(view.getChildAt(idx)))
                }
            }
        }
        return result
    }

    private fun select(filterElement: FilterElementView) {
        if (filterElement.picked) {
            presenter.addFilter(filterElement.filter)
        } else {
            presenter.removeFilter(filterElement.filter)
        }
    }

    fun setTagActionIcon(submit: Boolean) {
        ic_action.setImageResource(if (submit) R.drawable.ic_action_submit else R.drawable.ic_action_back_arrow)
    }

    fun setTagSuggestions(tags: List<Tag>) {
        suggestTagAdapter.updateData(tags.map { it.value })
    }

    fun setTags(tags: List<Tag>) {
        tagsAdapter.updateData(tags.map { it.value })
    }

    fun setAlbums(list: List<Album>) {
        albumAdapter.updateData(list)
    }

    fun selectAlbum(albumId: String) {
        if (albumId == albumAdapter.selectedAlbum) return
        val position = albumAdapter.getPosition(albumId)
        val selectedPosition = albumAdapter.getPosition(albumAdapter.selectedAlbum)
        setAlbumSelection(position, true)
        setAlbumSelection(selectedPosition, false)
        albumAdapter.selectedAlbum = albumId
    }

    private fun setAlbumSelection(position: Int, selected: Boolean) {
        if (position != -1) {
            (album_list.findViewHolderForAdapterPosition(position) as AlbumViewHolder).select(selected)
        }
    }

    fun clearView() {
        name_field.setText("")
        filterElements.filter { it.picked }.forEach { it.pick() }
        tag_field.setText("")
        setTags(emptyList())
        selectAlbum("")
        new_card_view.scrollTo(0, 0)
    }

    fun showPhotoParams() {
        choose_view.visibility = View.GONE
        new_card_view.visibility = View.VISIBLE
    }

    fun showPhotoChoose() {
        choose_view.visibility = View.VISIBLE
        new_card_view.visibility = View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        presenter.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        presenter.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}


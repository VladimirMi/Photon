package io.github.vladimirmi.photon.features.album

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.util.AttributeSet
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.data.models.EditAlbumReq
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.dto.PhotocardDto
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.features.main.CardAdapter
import io.github.vladimirmi.photon.features.main.CardViewHolder
import io.github.vladimirmi.photon.ui.EditAlbumDialog
import io.github.vladimirmi.photon.ui.SimpleDialog
import kotlinx.android.synthetic.main.screen_album.view.*


/**
 * Created by Vladimir Mikhalev 19.06.2017.
 */

class AlbumView(context: Context, attrs: AttributeSet)
    : BaseView<AlbumPresenter, AlbumView>(context, attrs) {

    val name by lazy { album_name }
    val description by lazy { album_description }
    private val cardCount by lazy { card_count }
    private val photocardList by lazy { photocard_list }

    private var editMode: Boolean = false

    private val cardAction: (PhotocardDto) -> Unit = {
        if (editMode) presenter.deletePhotocard(it) else presenter.showPhotoCard(it)
    }
    private val adapter = CardAdapter(cardAction, hideInfo = true)

    private val deleteAction: () -> Unit = { presenter.delete() }
    private val deleteDialog = SimpleDialog(this, R.string.dialog_delete_album, deleteAction)

    private val editAction: (EditAlbumReq) -> Unit = { presenter.editAlbum(it) }
    private val editDialog by lazy {
        EditAlbumDialog(this, editAction).also {
            it.initFields(name.text, description.text)
        }
    }

    override fun initDagger(context: Context) {
        DaggerService.getComponent<AlbumScreen.Component>(context).inject(this)
    }

    override fun onBackPressed(): Boolean {
        return presenter.onBackPressed()
    }

    override fun initView() {
        photocardList.layoutManager = GridLayoutManager(context, 3)
        photocardList.adapter = adapter
        photocardList.setOnLongClickListener { presenter.setEditable(true); true }
    }

    fun setAlbum(album: AlbumDto) {
        name.text = album.title
        description.text = album.description
        cardCount.text = album.photocards.size.toString()
        adapter.updateData(album.photocards)
    }

    fun setEditable(editMode: Boolean) {
        this.editMode = editMode
        (0..photocardList.adapter.itemCount - 1)
                .map { photocardList.findViewHolderForAdapterPosition(it) as CardViewHolder }
                .forEach { it.longTapAction(editMode) }

    }

    fun showDeleteDialog() = deleteDialog.show()
    fun closeDeleteDialog() = deleteDialog.hide()
    fun showEditDialog() = editDialog.show()
    fun closeEditDialog() = editDialog.hide()

    fun deletePhotocard(photocard: PhotocardDto) {
        adapter.deletePhotocard(photocard)
    }

    override fun onViewRestored() {
        super.onViewRestored()
        editDialog.subscribe()
    }

    override fun onViewDestroyed(removedByFlow: Boolean) {
        super.onViewDestroyed(removedByFlow)
        editDialog.unsubscribe()
    }
}


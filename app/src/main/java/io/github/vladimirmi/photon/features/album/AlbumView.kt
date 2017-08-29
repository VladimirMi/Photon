package io.github.vladimirmi.photon.features.album

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.util.AttributeSet
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseView
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

    private var editMode: Boolean = false
    private lateinit var album: AlbumDto

    private val cardAction: (PhotocardDto) -> Unit = {
        if (editMode) presenter.deletePhotocard(it) else presenter.showPhotoCard(it)
    }
    private val adapter = CardAdapter(cardAction, hideInfo = true)

    private val deleteAction: () -> Unit = { presenter.delete() }
    private val deleteDialog = SimpleDialog(this, R.string.dialog_delete_album, deleteAction)

    private val editAction: (AlbumDto) -> Unit = { presenter.editAlbum(it) }
    private val editDialog by lazy { EditAlbumDialog(this, editAction, album) }

    override fun initDagger(context: Context) {
        DaggerService.getComponent<AlbumScreen.Component>(context).inject(this)
    }

    override fun onBackPressed() = presenter.onBackPressed()

    override fun initView() {
        photocard_list.layoutManager = GridLayoutManager(context, 3)
        photocard_list.adapter = adapter
        photocard_list.setOnLongClickListener { presenter.setEditable(true); true }
    }

    fun setAlbum(album: AlbumDto) {
        this.album = album
        album_name.text = album.title
        album_description.text = album.description
        card_count.text = album.photocards.size.toString()
        adapter.updateData(album.photocards)
    }

    fun setEditable(editMode: Boolean) {
        this.editMode = editMode
        (0 until photocard_list.adapter.itemCount)
                .map { photocard_list.findViewHolderForAdapterPosition(it) as? CardViewHolder }
                .forEach { it?.longTapAction(editMode) }

    }

    fun showDeleteDialog() {
        deleteDialog.show()
    }

    fun closeDeleteDialog() {
        deleteDialog.hide()
    }

    fun showEditDialog() {
        editDialog.show()
        editDialog.subscribe()
    }

    fun closeEditDialog() {
        editDialog.hide()
        editDialog.unsubscribe()
    }

    fun deletePhotocard(photocard: PhotocardDto) {
        adapter.deletePhotocard(photocard)
    }
}


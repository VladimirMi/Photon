package io.github.vladimirmi.photon.features.album

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.util.AttributeSet
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.features.main.CardAdapter
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

    private val cardAction: (Photocard) -> Unit = {
        if (editMode) presenter.deletePhotocard(it) else presenter.showPhotoCard(it)
    }
    private val adapter = CardAdapter(cardAction, hideInfo = true)

    private val deleteAction: () -> Unit = { presenter.delete() }
    private val deleteDialog = SimpleDialog(this, R.string.dialog_delete_album, deleteAction)

    override fun initDagger(context: Context) {
        DaggerService.getComponent<AlbumScreen.Component>(context).inject(this)
    }

    override fun onBackPressed(): Boolean {
        return presenter.onBackPressed()
    }

    override fun initView() {
        photocardList.layoutManager = GridLayoutManager(context, 3)
        photocardList.adapter = adapter
        name.requestFocus()
    }

    fun setAlbum(album: Album) {
        name.setText(album.title)
        description.setText(album.description)
        cardCount.text = album.photocards.size.toString()
        adapter.updateData(album.photocards)
    }

    fun setEditable(editMode: Boolean) {
        this.editMode = editMode
        name.isEnabled = editMode
        if (editMode) {
            name.requestFocus()
            name.setSelection(name.length())
            description.setSelection(description.length())
        }
        description.isEnabled = editMode
        adapter.longTapAction = editMode
    }

    fun showDeleteDialog() = deleteDialog.show()
    fun closeDeleteDialog() = deleteDialog.hide()

    fun deletePhotocard(photocard: Photocard) {
        adapter.deletePhotocard(photocard)
    }
}


package io.github.vladimirmi.photon.presentation.newcard.album

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.util.AttributeSet
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.domain.models.AlbumDto
import io.github.vladimirmi.photon.presentation.main.AlbumAdapter
import io.github.vladimirmi.photon.presentation.main.AlbumViewHolder
import io.github.vladimirmi.photon.presentation.newcard.NewCardScreen
import kotlinx.android.synthetic.main.view_newcard_step3.view.*

/**
 * Created by Vladimir Mikhalev 28.07.2017.
 */

class NewCardAlbumView(context: Context, attrs: AttributeSet)
    : BaseView<NewCardAlbumPresenter, NewCardAlbumView>(context, attrs) {

    private val albumAction: (AlbumDto) -> Unit = { presenter.setAlbum(it) }
    private val albumAdapter = AlbumAdapter(albumAction)

    override fun initDagger(context: Context) {
        DaggerService.getComponent<NewCardScreen.Component>(context).inject(this)
    }

    override fun initView() {
        album_list.layoutManager = GridLayoutManager(context, 2)
        album_list.adapter = albumAdapter
    }

    fun setAlbums(list: List<AlbumDto>) = albumAdapter.updateData(list)

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
}
package io.github.vladimirmi.photon.features.album

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.util.AttributeSet
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.data.models.Album
import io.github.vladimirmi.photon.data.models.Photocard
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.features.main.CardAdapter
import kotlinx.android.synthetic.main.screen_album.view.*

/**
 * Created by Vladimir Mikhalev 19.06.2017.
 */

class AlbumView(context: Context, attrs: AttributeSet)
    : BaseView<AlbumPresenter, AlbumView>(context, attrs) {

    val cardAction: (Photocard) -> Unit = { presenter.showPhotoCard(it) }
    val adapter = CardAdapter(cardAction, hideInfo = true)

    override fun initDagger(context: Context) {
        DaggerService.getComponent<AlbumScreen.Component>(context).inject(this)
    }

    override fun initView() {
        photocard_list.layoutManager = GridLayoutManager(context, 3)
        photocard_list.adapter = adapter
    }

    fun setAlbum(album: Album) {
        album_name.text = album.title
        card_count.text = album.photocards.size.toString()
        album_description.text = album.description
        adapter.updateData(album.photocards)
    }
}


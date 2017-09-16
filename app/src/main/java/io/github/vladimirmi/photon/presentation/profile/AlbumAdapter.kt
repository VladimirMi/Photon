package io.github.vladimirmi.photon.presentation.main

import android.support.v7.util.DiffUtil
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.domain.models.AlbumDto
import io.github.vladimirmi.photon.utils.getDisplayMetrics
import io.github.vladimirmi.photon.utils.setImage
import kotlinx.android.synthetic.main.item_album.view.*
import kotlinx.android.synthetic.main.view_likes_views.view.*

/**
 * Developer Vladimir Mikhalev, 18.06.2017.
 */

class AlbumAdapter(val albumAction: (AlbumDto) -> Unit)
    : RecyclerView.Adapter<AlbumViewHolder>() {

    var authorMode = false
    var selectedAlbum = ""

    private var data: List<AlbumDto> = ArrayList()

    fun updateData(list: List<AlbumDto>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = data.size

            override fun getNewListSize() = list.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return data[oldItemPosition].id == list[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return data[oldItemPosition] == list[newItemPosition]
            }
        })
        data = list
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_album, parent, false)
        if (authorMode) view.likesNViews.visibility = GONE else view.menu_share.visibility = GONE
        view.layoutParams = view.layoutParams.apply {
            val spanCount = (((parent as RecyclerView).layoutManager) as GridLayoutManager).spanCount
            width = parent.context.getDisplayMetrics().widthPixels / spanCount
            height = width
        }
        return AlbumViewHolder(view, albumAction)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(data[position])
        if (data[position].id == selectedAlbum) holder.select(true)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun getPosition(albumId: String): Int {
        data.forEachIndexed { index, album -> if (album.id == albumId) return index }
        return -1
    }
}

@Suppress("HasPlatformType")
class AlbumViewHolder(itemView: View, val albumAction: (AlbumDto) -> Unit)
    : RecyclerView.ViewHolder(itemView) {

    val albumWrapper = itemView.album_wrapper
    val previewIm = itemView.preview
    val albumNameTxt = itemView.album_name
    val cardCountTxt = itemView.card_count
    val likesTxt = itemView.likes
    val viewsTxt = itemView.views

    fun bind(album: AlbumDto) {
        previewIm.setOnClickListener { albumAction(album) }
        albumNameTxt.text = album.title
        cardCountTxt.text = album.photocards.size.toString()
        likesTxt.text = album.photocards.fold(0, { acc, photocard -> acc + photocard.favorits }).toString()
        viewsTxt.text = album.photocards.fold(0, { acc, photocard -> acc + photocard.views }).toString()
        previewIm.setImage(album.photocards.getOrNull(0)?.photo)
    }

    fun select(selected: Boolean) {
        albumWrapper.setBackgroundResource(if (selected) R.drawable.album_selected else R.drawable.gradient_album)
    }
}

package io.github.vladimirmi.photon.features.main

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.utils.getDisplayMetrics
import io.github.vladimirmi.photon.utils.setImage
import kotlinx.android.synthetic.main.item_album.view.*
import kotlinx.android.synthetic.main.view_likes_views.view.*
import java.util.*

/**
 * Developer Vladimir Mikhalev, 18.06.2017.
 */

class AlbumAdapter(val albumAction: (Album) -> Unit)
    : RecyclerView.Adapter<AlbumViewHolder>() {

    var authorMode = false
    var selectedAlbum = ""

    private var data: List<Album> = ArrayList()

    fun updateData(list: List<Album>) {
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
        val lp = view.layoutParams
        lp.width = parent.context.getDisplayMetrics().widthPixels / 2
        lp.height = lp.width
        view.layoutParams = lp
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

class AlbumViewHolder(itemView: View, val albumAction: (Album) -> Unit)
    : RecyclerView.ViewHolder(itemView) {

    private var curImagePath = ""
    fun bind(album: Album) {
        itemView.preview.setOnClickListener { albumAction(album) }
        itemView.album_name.text = album.title

        val photocards = album.photocards.filter { it.active }
        itemView.card_count.text = photocards.size.toString()
        itemView.likes.text = photocards.fold(0, { acc, photocard -> acc + photocard.favorits }).toString()
        itemView.views.text = photocards.fold(0, { acc, photocard -> acc + photocard.views }).toString()

        if (photocards.isNotEmpty() && curImagePath != photocards[0].photo) {
            itemView.preview.setImage(photocards[0].photo)
            curImagePath = photocards[0].photo
        } else {
            itemView.preview.setImage("")
        }
    }

    fun select(selected: Boolean) {
        itemView.album_wrapper.setBackgroundResource(if (selected) R.drawable.album_selected else R.drawable.gradient_album)
    }
}

package io.github.vladimirmi.photon.features.main

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.data.models.Album
import io.github.vladimirmi.photon.ui.getDisplayMetrics
import io.github.vladimirmi.photon.ui.setImage
import kotlinx.android.synthetic.main.item_album.view.*
import kotlinx.android.synthetic.main.view_likes_views.view.*
import java.util.*

/**
 * Developer Vladimir Mikhalev, 18.06.2017.
 */

class AlbumAdapter(val albumAction: (Album) -> Unit)
    : RecyclerView.Adapter<AlbumViewHolder>() {

    var authorMode = false
    private var selectedAlbum = ""
    private var data: List<Album> = ArrayList()

    fun updateData(list: List<Album>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = data.size

            override fun getNewListSize() = list.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return data[oldItemPosition].id == list[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = true
        })
        data = list
        diffResult.dispatchUpdatesTo(this)
    }

    fun selectAlbum(album: Album) {
        selectedAlbum = album.id
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_album, parent, false)
        if (authorMode) view.likesNViews.visibility = GONE else view.menu_share.visibility = GONE
        val lp = view.layoutParams
        lp.width = getDisplayMetrics(parent.context).widthPixels / 2
        lp.height = lp.width
        view.layoutParams = lp
        return AlbumViewHolder(view, albumAction)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(data[position])
        if (data[position].id == selectedAlbum) holder.select()
    }

    override fun getItemCount(): Int {
        return data.size
    }
}

class AlbumViewHolder(itemView: View, val albumAction: (Album) -> Unit)
    : RecyclerView.ViewHolder(itemView) {

    private var curImagePath = ""
    fun bind(album: Album) {
        itemView.album_name.text = album.title
        itemView.card_count.text = album.photocards.size.toString()
        itemView.likes.text = album.favorits.toString()
        itemView.views.text = album.views.toString()
        itemView.album_wrapper.setBackgroundResource(R.drawable.album_gradient)
        itemView.preview.setOnClickListener { albumAction(album) }
        if (album.photocards.size > 0 && curImagePath != album.photocards[0].photo) {
            setImage(album.photocards[0].photo, itemView.preview)
            curImagePath = album.photocards[0].photo
        } else {
            setImage("", itemView.preview)
        }
    }

    fun select() {
        itemView.album_wrapper.setBackgroundResource(R.drawable.album_selected)
    }

}

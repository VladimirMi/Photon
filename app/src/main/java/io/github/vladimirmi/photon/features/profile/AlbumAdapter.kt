package io.github.vladimirmi.photon.features.main

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.data.models.Album
import io.github.vladimirmi.photon.ui.getDisplayMetrics
import io.github.vladimirmi.photon.ui.setImage
import kotlinx.android.synthetic.main.item_album.view.*
import java.util.*

/**
 * Developer Vladimir Mikhalev, 18.06.2017.
 */

class AlbumAdapter(val albumAction: (Album) -> Unit)
    : RecyclerView.Adapter<AlbumViewHolder>() {

    var data: List<Album> = ArrayList()

    fun updateData(data: List<Album>) {
        this.data = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_album, parent, false)
        val lp = view.layoutParams
        lp.width = getDisplayMetrics(parent.context).widthPixels / 2
        lp.height = lp.width
        view.layoutParams = lp
        return AlbumViewHolder(view, albumAction)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }
}

class AlbumViewHolder(itemView: View, val albumAction: (Album) -> Unit)
    : RecyclerView.ViewHolder(itemView) {

    fun bind(album: Album) {
        itemView.album_name.text = album.title
        itemView.card_count.text = album.photocards.size.toString()
        itemView.likes.text = album.favorits.toString()
        itemView.views.text = album.views.toString()
        itemView.preview.setOnClickListener { albumAction(album) }
        if (!album.preview.isEmpty()) {
            setImage(album.preview, itemView.preview)
        } else if (album.photocards.size > 0) {
            setImage(album.photocards[0].photo, itemView.preview)
        } else {
            setImage("", itemView.preview)
        }
    }

}

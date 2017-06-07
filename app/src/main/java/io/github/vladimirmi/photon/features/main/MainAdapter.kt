package io.github.vladimirmi.photon.features.main

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.data.network.models.Photocard
import io.github.vladimirmi.photon.utils.setImage
import kotlinx.android.synthetic.main.item_card.view.*
import java.util.*

/**
 * Developer Vladimir Mikhalev, 03.06.2017.
 */

class MainAdapter : RecyclerView.Adapter<CardViewHolder>() {

    var data: List<Photocard> = ArrayList()

    fun updateData(data: List<Photocard>) {
        this.data = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_card, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }
}

class CardViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
    fun bind(photoCard: Photocard) {
        itemView.likes.text = photoCard.favorits.toString()
        itemView.views.text = photoCard.views.toString()
        setImage(photoCard.photo, itemView.photo_card)
    }

}

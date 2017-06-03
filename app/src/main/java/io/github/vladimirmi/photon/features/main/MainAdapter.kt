package io.github.vladimirmi.photon.features.main

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.data.network.models.PhotocardRes

/**
 * Developer Vladimir Mikhalev, 03.06.2017.
 */

class MainAdapter : RecyclerView.Adapter<CardViewHolder>() {

    var data: List<PhotocardRes>? = null

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CardViewHolder {
        val inflater = LayoutInflater.from(parent?.context)
        val view = inflater.inflate(R.layout.item_card, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder?, position: Int) {
        holder?.bind(data?.get(position))
    }

    override fun getItemCount(): Int {
        return data?.size ?: 0
    }
}

class CardViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
    fun bind(photoCard: PhotocardRes?) {
        // bind view
    }

}

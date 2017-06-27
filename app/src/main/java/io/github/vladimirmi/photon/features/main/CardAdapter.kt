package io.github.vladimirmi.photon.features.main

import android.support.v7.util.DiffUtil
import android.support.v7.util.DiffUtil.calculateDiff
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.ui.getDisplayMetrics
import io.github.vladimirmi.photon.ui.setImage
import kotlinx.android.synthetic.main.item_photocard.view.*
import kotlinx.android.synthetic.main.view_likes_views.view.*
import java.util.*

/**
 * Developer Vladimir Mikhalev, 03.06.2017.
 */

class CardAdapter(private val cardAction: (Photocard) -> Unit, private val hideInfo: Boolean = false)
    : RecyclerView.Adapter<CardViewHolder>() {

    private var data: List<Photocard> = ArrayList()

    fun updateData(list: List<Photocard>) {
        val diffResult = calculateDiff(object : DiffUtil.Callback() {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_photocard, parent, false)
        if (hideInfo) view.info.visibility = GONE
        val lp = view.layoutParams
        val spanCount = (((parent as RecyclerView).layoutManager) as GridLayoutManager).spanCount
        lp.width = getDisplayMetrics(parent.context).widthPixels / spanCount
        lp.height = lp.width
        view.layoutParams = lp
        return CardViewHolder(view, cardAction)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }
}

class CardViewHolder(itemView: View?, val cardAction: (Photocard) -> Unit) : RecyclerView.ViewHolder(itemView) {

    private var curImagePath = ""
    fun bind(photoCard: Photocard) {
        itemView.likes.text = photoCard.favorits.toString()
        itemView.views.text = photoCard.views.toString()
        itemView.photo_card.setOnClickListener { cardAction(photoCard) }
        if (curImagePath != photoCard.photo) {
            setImage(photoCard.photo, itemView.photo_card)
            curImagePath = photoCard.photo
        }
    }

}

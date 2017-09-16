package io.github.vladimirmi.photon.presentation.main

import android.support.v7.util.DiffUtil
import android.support.v7.util.DiffUtil.calculateDiff
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.domain.models.PhotocardDto
import io.github.vladimirmi.photon.utils.getDisplayMetrics
import io.github.vladimirmi.photon.utils.setImage
import kotlinx.android.synthetic.main.item_photocard.view.*
import kotlinx.android.synthetic.main.view_likes_views.view.*
import java.util.*

/**
 * Developer Vladimir Mikhalev, 03.06.2017.
 */

class CardAdapter(private val cardAction: (PhotocardDto) -> Unit, private val hideInfo: Boolean = false)
    : RecyclerView.Adapter<CardViewHolder>() {

    private var data: List<PhotocardDto> = ArrayList()

    fun updateData(list: List<PhotocardDto>) {
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
        view.layoutParams = view.layoutParams.apply {
            val spanCount = (((parent as RecyclerView).layoutManager) as GridLayoutManager).spanCount
            width = parent.context.getDisplayMetrics().widthPixels / spanCount
            height = width
        }
        return CardViewHolder(view, cardAction)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun deletePhotocard(photocard: PhotocardDto) {
        updateData(data.filter { it.id != photocard.id })
    }
}

class CardViewHolder(itemView: View?, val cardAction: (PhotocardDto) -> Unit) : RecyclerView.ViewHolder(itemView) {

    private var curImagePath = ""
    fun bind(photoCard: PhotocardDto) {
        itemView.long_tap_action.visibility = GONE
        itemView.likes.text = photoCard.favorits.toString()
        itemView.views.text = photoCard.views.toString()

        if (curImagePath != photoCard.photo) {
            itemView.photo_card.setImage(photoCard.photo)
            curImagePath = photoCard.photo
        }

        itemView.photo_card.setOnClickListener { cardAction(photoCard) }
        itemView.photo_card.setOnLongClickListener { (itemView.parent as View).performLongClick(); true }
    }

    fun longTapAction(enabled: Boolean) {
        itemView.long_tap_action.visibility = if (enabled) VISIBLE else GONE
    }
}

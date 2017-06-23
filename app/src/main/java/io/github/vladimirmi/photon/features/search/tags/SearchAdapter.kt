package io.github.vladimirmi.photon.features.search.tags

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.vladimirmi.photon.R
import kotlinx.android.synthetic.main.item_simple_string.view.*

/**
 * Created by Vladimir Mikhalev 23.06.2017.
 */

class SearchAdapter : RecyclerView.Adapter<ItemViewHolder>() {

    var searchResult: List<String> = ArrayList()

    fun updateData(list: List<String>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = searchResult.size

            override fun getNewListSize() = list.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return searchResult[oldItemPosition] == list[newItemPosition]
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = true
        })
        searchResult = list
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_simple_string, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.textView.text = searchResult[position]
    }

    override fun getItemCount() = searchResult.size
}

class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val textView = itemView.text_view
}

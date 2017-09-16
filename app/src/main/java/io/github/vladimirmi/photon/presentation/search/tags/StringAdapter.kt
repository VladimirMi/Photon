package io.github.vladimirmi.photon.presentation.search.tags

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.github.vladimirmi.photon.R
import kotlinx.android.synthetic.main.item_simple_string.view.*

/**
 * Created by Vladimir Mikhalev 23.06.2017.
 */

class StringAdapter(private val action: ((String) -> Unit)? = null) : RecyclerView.Adapter<ItemViewHolder>() {

    private var strings = ArrayList<String>()

    fun updateData(list: List<String>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = strings.size

            override fun getNewListSize() = list.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return strings[oldItemPosition] == list[newItemPosition]
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = true
        })
        strings.clear()
        strings.addAll(list)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_simple_string, parent, false)
        return ItemViewHolder(view, action)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.textView.text = strings[position]
    }

    override fun getItemCount() = strings.size
}

class ItemViewHolder(itemView: View, action: ((String) -> Unit)? = null) : RecyclerView.ViewHolder(itemView) {
    val textView: TextView = itemView.text_view

    init {
        textView.setOnClickListener { action?.invoke((it as TextView).text.toString()) }
    }
}

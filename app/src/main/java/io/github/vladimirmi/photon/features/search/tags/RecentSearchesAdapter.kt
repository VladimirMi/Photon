package io.github.vladimirmi.photon.features.search.tags

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.github.vladimirmi.photon.data.models.Search

/**
 * Created by Vladimir Mikhalev 09.06.2017.
 */

class RecentSearchesAdapter : RecyclerView.Adapter<SearchViewHolder>() {
    var data: List<Search> = ArrayList()

    fun updateData(data: List<Search>) {
        this.data = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        return SearchViewHolder(TextView(parent.context))
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size
}

class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(search: Search) {
        if (itemView is TextView) itemView.text = search.value
    }
}

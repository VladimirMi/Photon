package io.github.vladimirmi.photon.ui

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import io.github.vladimirmi.photon.utils.AppConfig


/**
 * Created by Vladimir Mikhalev 13.07.2017.
 */

abstract class EndlessRecyclerViewScrollListener(private val layoutManager: GridLayoutManager)
    : RecyclerView.OnScrollListener() {

    private val limit = AppConfig.PHOTOCARDS_PAGE_SIZE
    private val threshold = limit / 2

    private var loadedPage = 0
    private var previousTotalItemCount = 0
    private var loading = true


    override fun onScrolled(view: RecyclerView, dx: Int, dy: Int) {
        val totalItemCount = layoutManager.itemCount
        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

        if (loading && totalItemCount > previousTotalItemCount) {
            loading = false
            previousTotalItemCount = totalItemCount
        }

        if (!loading && lastVisibleItemPosition + threshold > limit * (loadedPage + 1)) {
            loadedPage++
            onLoadMore(loadedPage, limit, view)
            loading = true
        }
    }

    fun resetState() {
        this.loadedPage = 0
        this.previousTotalItemCount = 0
        this.loading = true
    }

    abstract fun onLoadMore(page: Int, limit: Int, view: RecyclerView)

}
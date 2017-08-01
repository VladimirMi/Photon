package io.github.vladimirmi.photon.features.main

import io.github.vladimirmi.photon.data.jobs.queue.PhotocardJobQueue
import io.github.vladimirmi.photon.data.managers.Cache
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.dto.PhotocardDto
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.features.search.SearchView
import io.github.vladimirmi.photon.utils.JobStatus
import io.github.vladimirmi.photon.utils.Query
import io.github.vladimirmi.photon.utils.ioToMain
import io.reactivex.Observable
import io.realm.Sort

/**
 * Developer Vladimir Mikhalev, 03.06.2017.
 */

class MainModel(val dataManager: DataManager,
                val photocardJobQueue: PhotocardJobQueue,
                val cache: Cache) : IMainModel {

    var query = ArrayList<Query>()
    override var queryPage = SearchView.Page.TAGS
    override var tagsQuery = ArrayList<Query>()
    override var filtersQuery = ArrayList<Query>()


    override fun makeQuery() {
        when (queryPage) {
            SearchView.Page.TAGS -> query = tagsQuery
            SearchView.Page.FILTERS -> query = filtersQuery
        }
    }

    override fun getPhotoCards(): Observable<List<PhotocardDto>> {
        return dataManager.search(Photocard::class.java,
                query = if (query.isNotEmpty()) query.toList() else null,
                sortBy = "views",
                order = Sort.DESCENDING)
                .map { cache.cachePhotos(it) }
                .ioToMain()
    }

    override fun isFiltered() = query.isNotEmpty()

    override fun resetFilter() {
        query.clear()
        tagsQuery.clear()
        filtersQuery.clear()
        queryPage = SearchView.Page.TAGS
    }

    override fun addView(photocardId: String): Observable<JobStatus> {
        return photocardJobQueue.queueAddViewJob(photocardId)
                .ioToMain()
    }

    override fun updatePhotocards(offset: Int, limit: Int): Observable<Unit> {
        return dataManager.isNetworkAvailable()
                .filter { it }
                .flatMap { dataManager.getPhotocardsFromNet(offset, limit) }
                .map { it.forEach { dataManager.saveToDB(it) } }
                .ioToMain()
    }
}

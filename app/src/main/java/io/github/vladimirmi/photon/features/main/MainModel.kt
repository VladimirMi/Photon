package io.github.vladimirmi.photon.features.main

import com.birbit.android.jobqueue.JobManager
import io.github.vladimirmi.photon.data.jobs.AddViewJob
import io.github.vladimirmi.photon.data.managers.Cache
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.dto.PhotocardDto
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.features.search.SearchView
import io.github.vladimirmi.photon.utils.Query
import io.github.vladimirmi.photon.utils.ioToMain
import io.reactivex.Observable
import io.realm.Sort

/**
 * Developer Vladimir Mikhalev, 03.06.2017.
 */

class MainModel(val dataManager: DataManager, val jobManager: JobManager, val cache: Cache) : IMainModel {

    var query = ArrayList<Query>()
    override var queryPage: SearchView.Page = SearchView.Page.TAGS
    override val tagsQuery = ArrayList<Query>()
    override val filtersQuery = ArrayList<Query>()


    override fun makeQuery(currentPage: SearchView.Page) {
        when (currentPage) {
            SearchView.Page.TAGS -> query = tagsQuery
            SearchView.Page.FILTERS -> query = filtersQuery
        }
        queryPage = currentPage
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

    override fun addView(photocardId: String) {
        jobManager.addJobInBackground(AddViewJob(photocardId))
    }

    override fun updatePhotocards(offset: Int, limit: Int): Observable<Unit> {
        return dataManager.getPhotocardsFromNet(offset, limit)
                .map { it.forEach { dataManager.saveToDB(it) } }
                .ioToMain()
    }
}

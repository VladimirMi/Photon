package io.github.vladimirmi.photon.domain.interactors

import io.github.vladimirmi.photon.data.managers.extensions.JobStatus
import io.github.vladimirmi.photon.data.repository.photocard.PhotocardRepository
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.domain.mappers.PhotocardCachingMapper
import io.github.vladimirmi.photon.domain.models.PhotocardDto
import io.github.vladimirmi.photon.presentation.main.MainInteractor
import io.github.vladimirmi.photon.presentation.main.MainScreen
import io.github.vladimirmi.photon.presentation.search.SearchView
import io.github.vladimirmi.photon.utils.Query
import io.github.vladimirmi.photon.utils.ioToMain
import io.reactivex.Completable
import io.reactivex.Observable
import io.realm.Sort
import javax.inject.Inject

/**
 * Developer Vladimir Mikhalev, 03.06.2017.
 */

@DaggerScope(MainScreen::class)
class MainInteractorImpl
@Inject constructor(private val photocardRepository: PhotocardRepository,
                    private val photocardMapper: PhotocardCachingMapper)
    : MainInteractor {

    var query = ArrayList<Query>()
    override var queryPage = SearchView.Page.TAGS
    override var tagsQuery = ArrayList<Query>()
    override var filtersQuery = ArrayList<Query>()

    override fun makeQuery() {
        query = when (queryPage) {
            SearchView.Page.TAGS -> tagsQuery
            SearchView.Page.FILTERS -> filtersQuery
        }
    }

    override fun getPhotoCards(): Observable<List<PhotocardDto>> {
        return photocardRepository.getPhotocards(
                query = if (query.isNotEmpty()) query.toList() else null,
                sortBy = "views",
                order = Sort.DESCENDING)
                .map { photocardMapper.map(it) }
                .ioToMain()
    }

    override fun isFiltered() = query.isNotEmpty()

    override fun resetFilter() {
        query.clear()
        tagsQuery.clear()
        filtersQuery.clear()
        queryPage = SearchView.Page.TAGS
    }

    override fun addView(photocardId: String): Observable<JobStatus> =
            photocardRepository.addView(photocardId).ioToMain()

    override fun updatePhotocards(offset: Int, limit: Int): Completable =
            photocardRepository.updatePhotocards(offset, limit).ioToMain()
}

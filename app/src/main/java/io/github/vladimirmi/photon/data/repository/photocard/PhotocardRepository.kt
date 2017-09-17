package io.github.vladimirmi.photon.data.repository.photocard

import com.birbit.android.jobqueue.JobManager
import io.github.vladimirmi.photon.core.App
import io.github.vladimirmi.photon.data.jobs.photocard.PhotocardAddViewJob
import io.github.vladimirmi.photon.data.jobs.photocard.PhotocardCreateJob
import io.github.vladimirmi.photon.data.jobs.photocard.PhotocardDeleteJob
import io.github.vladimirmi.photon.data.managers.PreferencesManager
import io.github.vladimirmi.photon.data.managers.RealmManager
import io.github.vladimirmi.photon.data.managers.utils.JobStatus
import io.github.vladimirmi.photon.data.managers.utils.Query
import io.github.vladimirmi.photon.data.managers.utils.addAndObserve
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.network.NetworkChecker
import io.github.vladimirmi.photon.data.network.api.RestService
import io.github.vladimirmi.photon.data.network.parseGetResponse
import io.github.vladimirmi.photon.di.DaggerScope
import io.reactivex.Completable
import io.reactivex.Observable
import io.realm.Sort
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 04.09.2017.
 */

@DaggerScope(App::class)
class PhotocardRepository
@Inject constructor(realmManager: RealmManager,
                    private val restService: RestService,
                    private val preferencesManager: PreferencesManager,
                    private val networkChecker: NetworkChecker,
                    private val jobManager: JobManager)
    : PhotocardEntityRepository(realmManager) {

    fun getPhotocards(query: List<Query>? = null,
                      sortBy: String? = null,
                      order: Sort = Sort.ASCENDING,
                      managed: Boolean = true): Observable<List<Photocard>> =
            realmManager.search(Photocard::class.java, query, sortBy, order, managed)

    fun updatePhotocards(offset: Int, limit: Int): Completable {
        val tag = Photocard::class.java.simpleName
        val lastModified = preferencesManager.getLastUpdate(tag)
        return networkChecker.singleAvailable()
                .flatMap { restService.getPhotocards(offset, limit, lastModified) }
                .parseGetResponse { preferencesManager.saveLastUpdate(tag, it) }
                .doOnSuccess { saveFromNet(it) }
                .ignoreElement()
    }

    fun getPhotocard(id: String, managed: Boolean = true): Observable<Photocard> =
            realmManager.getObject(Photocard::class.java, id, managed)


    fun updatePhotocard(id: String): Completable {
        val lastModified = realmManager.getLastUpdated(Photocard::class.java, id)
        return networkChecker.singleAvailable()
                .flatMap { restService.getPhotocard(id, "any", lastModified) }
                .parseGetResponse()
                .doOnSuccess { saveFromNet(it) }
                .ignoreElement()
    }

    fun create(photocard: Photocard): Observable<JobStatus> =
            Observable.fromCallable { photocard.create() }.
                    flatMap { jobManager.addAndObserve(PhotocardCreateJob(photocard.id)) }

    fun delete(id: String): Observable<JobStatus> =
            Observable.fromCallable { getPhotocard(id).delete() }
                    .flatMap { jobManager.addAndObserve(PhotocardDeleteJob(id)) }

    fun addView(id: String): Observable<JobStatus> =
            Observable.fromCallable { getPhotocard(id).addView() }
                    .flatMap { jobManager.addAndObserve(PhotocardAddViewJob(id)) }
}


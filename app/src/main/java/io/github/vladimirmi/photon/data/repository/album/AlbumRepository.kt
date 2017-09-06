package io.github.vladimirmi.photon.data.repository.album

import io.github.vladimirmi.photon.core.App
import io.github.vladimirmi.photon.data.jobs.JobsManager
import io.github.vladimirmi.photon.data.jobs.album.*
import io.github.vladimirmi.photon.data.managers.PreferencesManager
import io.github.vladimirmi.photon.data.managers.RealmManager
import io.github.vladimirmi.photon.data.managers.extensions.JobStatus
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.req.AlbumEditReq
import io.github.vladimirmi.photon.data.network.NetworkChecker
import io.github.vladimirmi.photon.data.network.api.RestService
import io.github.vladimirmi.photon.data.network.parseGetResponse
import io.github.vladimirmi.photon.di.DaggerScope
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 05.09.2017.
 */

@DaggerScope(App::class)
class AlbumRepository
@Inject constructor(realmManager: RealmManager,
                    preferencesManager: PreferencesManager,
                    private val restService: RestService,
                    private val networkChecker: NetworkChecker,
                    private val jobsManager: JobsManager)
    : AlbumEntityRepository(realmManager, preferencesManager) {

    fun getAlbum(id: String, managed: Boolean = true): Observable<Album> =
            realmManager.getObject(Album::class.java, id, managed)

    fun updateAlbum(id: String): Completable {
        if (!jobsManager.isSync(id)) return Completable.complete()
        val lastModified = realmManager.getLastUpdated(Album::class.java, id)
        return networkChecker.singleAvailable()
                .flatMap { restService.getAlbum(id, "any", lastModified) }
                .parseGetResponse()
                .doOnSuccess { saveFromNet(it) }
                .ignoreElement()
    }


    fun create(album: Album): Observable<JobStatus> =
            Observable.fromCallable { album.create() }.
                    flatMap { jobsManager.observe(AlbumCreateJob.TAG + album.id) }

    fun delete(id: String): Observable<JobStatus> =
            Observable.fromCallable { getAlbum(id).delete() }
                    .flatMap { jobsManager.observe(AlbumDeleteJob.TAG + id) }

    fun edit(request: AlbumEditReq): Observable<JobStatus> =
            Observable.fromCallable { getAlbum(request.id).edit(request) }
                    .flatMap { jobsManager.observe(AlbumEditJob.TAG + request.id) }

    fun addFavorite(id: String): Observable<JobStatus> =
            Observable.fromCallable { getAlbum(preferencesManager.getFavAlbumId()).addFavorite(id) }
                    .flatMap { jobsManager.observe(AlbumAddFavoritePhotoJob.TAG + id) }

    fun deleteFavorite(id: String): Observable<JobStatus> =
            Observable.fromCallable { getAlbum(preferencesManager.getFavAlbumId()).deleteFavorite(id) }
                    .flatMap { jobsManager.observe(AlbumDeleteFavoritePhotoJob.TAG + id) }
}
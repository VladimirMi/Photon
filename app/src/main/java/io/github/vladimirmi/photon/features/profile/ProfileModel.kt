package io.github.vladimirmi.photon.features.profile

import io.github.vladimirmi.photon.data.jobs.queue.AlbumJobQueue
import io.github.vladimirmi.photon.data.jobs.queue.ProfileJobQueue
import io.github.vladimirmi.photon.data.managers.Cache
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.dto.UserDto
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.data.models.req.EditProfileReq
import io.github.vladimirmi.photon.data.models.req.NewAlbumReq
import io.github.vladimirmi.photon.utils.*
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.*

class ProfileModel(val dataManager: DataManager,
                   val albumJobQueue: AlbumJobQueue,
                   val profileJobQueue: ProfileJobQueue,
                   val cache: Cache)
    : IProfileModel {

    override fun isUserAuth(): Boolean {
        return dataManager.isUserAuth()
    }

    override fun getProfile(): Observable<UserDto> {
        val id = dataManager.getProfileId()
        updateUser(id)
        val profile = dataManager.getObjectFromDb(User::class.java, id)
                .flatMap { justOrEmpty(cache.cacheUser(it)) }
                .ioToMain()

        return Observable.merge(justOrEmpty(cache.user(id)), profile)
    }

    override fun getAlbums(): Observable<List<AlbumDto>> {
        val query = listOf(Query("owner", RealmOperator.EQUALTO, dataManager.getProfileId()))
        val albums = dataManager.search(Album::class.java, query)
                .map { cache.cacheAlbums(it) }

        return Observable.merge(Observable.just(cache.albums), albums).ioToMain()
    }

    private fun updateUser(id: String) {
        dataManager.isNetworkAvailable()
                .filter { it }
                .flatMap { Observable.just(dataManager.getDetachedObjFromDb(User::class.java, id)?.updated ?: Date(0)) }
                .flatMap { dataManager.getUserFromNet(id, getUpdated(it)) }
                .doOnNext { dataManager.saveToDB(it) }
                .subscribeOn(Schedulers.io())
                .subscribeWith(ErrorObserver())
    }

    override fun createAlbum(newAlbumReq: NewAlbumReq): Observable<JobStatus> {
        return albumJobQueue.queueCreateJob(newAlbumReq)
                .ioToMain()
    }

    override fun editProfile(profileReq: EditProfileReq): Observable<JobStatus> {
        return profileJobQueue.queueEditJob(profileReq)
                .ioToMain()
    }
}
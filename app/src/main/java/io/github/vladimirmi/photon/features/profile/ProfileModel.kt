package io.github.vladimirmi.photon.features.profile

import com.birbit.android.jobqueue.JobManager
import com.birbit.android.jobqueue.TagConstraint
import io.github.vladimirmi.photon.data.jobs.CreateAlbumJob
import io.github.vladimirmi.photon.data.jobs.EditProfileJob
import io.github.vladimirmi.photon.data.jobs.singleCancelJobs
import io.github.vladimirmi.photon.data.jobs.singleResultFor
import io.github.vladimirmi.photon.data.managers.Cache
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.EditProfileReq
import io.github.vladimirmi.photon.data.models.NewAlbumReq
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.dto.UserDto
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.utils.*
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.*

class ProfileModel(val dataManager: DataManager, val jobManager: JobManager, val cache: Cache)
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
        val albums = dataManager.search(Album::class.java, query, sortBy = "id")
                .map { cache.cacheAlbums(it) }

        return Observable.merge(Observable.just(cache.albums), albums).ioToMain()
    }

    private fun updateUser(id: String) {
        Observable.just(dataManager.getDetachedObjFromDb(User::class.java, id)?.updated ?: Date(0))
                .flatMap { dataManager.getUserFromNet(id, getUpdated(it)) }
                .doOnNext { dataManager.saveToDB(it) }
                .subscribeOn(Schedulers.io())
                .subscribeWith(ErrorObserver())
    }

    override fun createAlbum(newAlbumReq: NewAlbumReq): Single<Unit> {
        newAlbumReq.id = UUID.randomUUID().toString()
        newAlbumReq.owner = dataManager.getProfileId()

        val job = CreateAlbumJob(newAlbumReq)
        jobManager.addJobInBackground(job)
        return jobManager.singleResultFor(job)
                .ioToMain()
    }


    override fun editProfile(profileReq: EditProfileReq, loadAvatar: Boolean): Single<Unit> {
        val job = EditProfileJob(profileReq, loadAvatar = loadAvatar)

        return jobManager.singleCancelJobs(TagConstraint.ANY, job.tag)
                .doOnSuccess { jobManager.addJobInBackground(job) }
                .flatMap { jobManager.singleResultFor(job) }
                .ioToMain()
    }
}
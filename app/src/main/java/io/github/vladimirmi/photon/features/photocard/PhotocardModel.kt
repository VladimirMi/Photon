package io.github.vladimirmi.photon.features.photocard

import com.birbit.android.jobqueue.JobManager
import com.birbit.android.jobqueue.TagConstraint
import io.github.vladimirmi.photon.data.jobs.AddToFavoriteJob
import io.github.vladimirmi.photon.data.jobs.DeleteFromFavoriteJob
import io.github.vladimirmi.photon.data.jobs.singleCancelJobs
import io.github.vladimirmi.photon.data.jobs.singleResultFor
import io.github.vladimirmi.photon.data.managers.Cache
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.dto.PhotocardDto
import io.github.vladimirmi.photon.data.models.dto.UserDto
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.utils.*
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * Created by Vladimir Mikhalev 14.06.2017.
 */

class PhotocardModel(val dataManager: DataManager, val jobManager: JobManager, val cache: Cache) : IPhotocardModel {

    override fun getUser(id: String): Observable<UserDto> {
        updateUser(id)
        val user = dataManager.getObjectFromDb(User::class.java, id)
                .flatMap { justOrEmpty(cache.cacheUser(it)) }

        return Observable.merge(justOrEmpty(cache.user(id)), user).notNull().ioToMain()
    }

    private fun updateUser(id: String) {
        Observable.just(dataManager.getDetachedObjFromDb(User::class.java, id)?.updated ?: Date(0))
                .flatMap { dataManager.getUserFromNet(id, getUpdated(it)) }
                .doOnNext { dataManager.saveToDB(it) }
                .subscribeOn(Schedulers.io())
                .subscribeWith(ErrorObserver())
    }

    override fun getPhotocard(id: String, ownerId: String): Observable<PhotocardDto> {
        updatePhotocard(id, ownerId)
        val photocard = dataManager.getObjectFromDb(Photocard::class.java, id)
                .flatMap { justOrEmpty(cache.cachePhotocard(it)) }

        return Observable.merge(justOrEmpty(cache.photocard(id)), photocard).notNull().ioToMain()
    }

    private fun updatePhotocard(id: String, ownerId: String) {
        Observable.just(dataManager.getDetachedObjFromDb(Photocard::class.java, id)?.updated ?: Date(0))
                .flatMap { dataManager.getPhotocardFromNet(id, ownerId, getUpdated(it)) }
                .doOnNext { dataManager.saveToDB(it) }
                .subscribeOn(Schedulers.io())
                .subscribeWith(ErrorObserver())
    }

    override fun addToFavorite(id: String): Single<Unit> {
        val job = AddToFavoriteJob(id, favAlbumId!!)
        return jobManager.singleCancelJobs(TagConstraint.ANY, DeleteFromFavoriteJob.TAG + id)
                .doOnSuccess { jobManager.addJobInBackground(job) }
                .flatMap { jobManager.singleResultFor(job) }
                .ioToMain()
    }

    override fun removeFromFavorite(id: String): Single<Unit> {
        val job = DeleteFromFavoriteJob(id, favAlbumId!!)
        return jobManager.singleCancelJobs(TagConstraint.ANY, AddToFavoriteJob.TAG + id)
                .doOnSuccess { jobManager.addJobInBackground(job) }
                .flatMap { jobManager.singleResultFor(job) }
                .ioToMain()
    }


    override fun isFavorite(id: String): Observable<Boolean> {
        val favorite = getFavAlbum()
                .map { it.photocards.find { it.id == id } != null }

        return Observable.merge(Observable.just(checkFavorite(id)), favorite).ioToMain()
    }

    private fun checkFavorite(id: String): Boolean {
        if (!dataManager.isUserAuth()) return false
        val favPhoto = cache.albums.find { it.isFavorite }
                ?.also { favAlbumId = it.id }
                ?.photocards?.find { it.id == id }
        return favPhoto?.let { true } ?: false
    }

    private var favAlbumId: String? = null

    private fun getFavAlbum(): Observable<Album> {
        val query = listOf(
                Query("owner", RealmOperator.EQUALTO, dataManager.getProfileId()),
                Query("isFavorite", RealmOperator.EQUALTO, true)
        )
        return dataManager.search(Album::class.java, query, "id")
                .take(1)
                .map { it[0] }
                .doOnNext { favAlbumId = it.id }
    }
}
package io.github.vladimirmi.photon.features.photocard

import io.github.vladimirmi.photon.data.jobs.queue.PhotocardJobQueue
import io.github.vladimirmi.photon.data.managers.Cache
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.dto.PhotocardDto
import io.github.vladimirmi.photon.data.models.dto.UserDto
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.utils.*
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * Created by Vladimir Mikhalev 14.06.2017.
 */

class PhotocardModel(val dataManager: DataManager,
                     val photocardJobQueue: PhotocardJobQueue,
                     val cache: Cache) : IPhotocardModel {

    override fun getUser(id: String): Observable<UserDto> {
        updateUser(id)
        val user = dataManager.getObjectFromDb(User::class.java, id)
                .flatMap { justOrEmpty(cache.cacheUser(it)) }

        return Observable.merge(justOrEmpty(cache.user(id)), user).notNull().ioToMain()
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

    override fun getPhotocard(id: String, ownerId: String): Observable<PhotocardDto> {
        updatePhotocard(id, ownerId)
        val photocard = dataManager.getObjectFromDb(Photocard::class.java, id)
                .flatMap { justOrEmpty(cache.cachePhotocard(it)) }

        return Observable.merge(justOrEmpty(cache.photocard(id)), photocard).notNull().ioToMain()
    }

    private fun updatePhotocard(id: String, ownerId: String) {
        dataManager.isNetworkAvailable()
                .filter { it }
                .flatMap { Observable.just(dataManager.getDetachedObjFromDb(Photocard::class.java, id)?.updated ?: Date(0)) }
                .flatMap { dataManager.getPhotocardFromNet(id, ownerId, getUpdated(it)) }
                .doOnNext { dataManager.saveToDB(it) }
                .subscribeOn(Schedulers.io())
                .subscribeWith(ErrorObserver())
    }

    override fun addToFavorite(id: String): Observable<JobStatus> {
        return photocardJobQueue.queueAddToFavoriteJob(id)
                .ioToMain()
    }

    override fun removeFromFavorite(id: String): Observable<JobStatus> {
        return photocardJobQueue.queueDeleteFromFavoriteJob(id)
                .ioToMain()
    }


    override fun isFavorite(id: String): Observable<Boolean> {
        val favorite = dataManager.getObjectFromDb(Album::class.java, dataManager.getUserFavAlbumId())
                .map { it.photocards.find { it.id == id } != null }

        return Observable.merge(Observable.just(checkIsFavorite(id)), favorite).ioToMain()
    }


    private fun checkIsFavorite(id: String): Boolean {
        if (!dataManager.isUserAuth()) return false
        val favPhoto = cache.albums.find { it.isFavorite }
                ?.photocards?.find { it.id == id }
        return favPhoto?.let { true } ?: false
    }
}
package io.github.vladimirmi.photon.features.photocard

import io.github.vladimirmi.photon.data.jobs.queue.Jobs
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.dto.PhotocardDto
import io.github.vladimirmi.photon.data.models.dto.UserDto
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.utils.JobStatus
import io.github.vladimirmi.photon.utils.ioToMain
import io.reactivex.Completable
import io.reactivex.Observable

/**
 * Created by Vladimir Mikhalev 14.06.2017.
 */

class PhotocardModel(private val dataManager: DataManager,
                     private val jobs: Jobs) : IPhotocardModel {

    override fun getUser(id: String): Observable<UserDto> {
        return dataManager.getCached<User, UserDto>(id)
                .ioToMain()
    }

    override fun updateUser(id: String): Completable {
        return dataManager.isNetworkAvailable()
                .filter { it }
                .flatMap { dataManager.getUserFromNet(id) }
                .doOnNext { dataManager.saveToDB(it) }
                .ignoreElements()
                .ioToMain()
    }

    override fun getPhotocard(id: String): Observable<PhotocardDto> {
        return dataManager.getCached<Photocard, PhotocardDto>(id)
                .ioToMain()
    }

    override fun updatePhotocard(id: String, ownerId: String): Completable {
        return dataManager.isNetworkAvailable()
                .filter { it }
                .flatMap { dataManager.getPhotocardFromNet(id, ownerId) }
                .doOnNext { dataManager.saveToDB(it) }
                .ignoreElements()
                .ioToMain()
    }

    override fun addToFavorite(id: String): Observable<JobStatus> =
            jobs.albumAddFavorite(id).ioToMain()

    override fun removeFromFavorite(id: String): Observable<JobStatus> =
            jobs.albumDeleteFavorite(id).ioToMain()


    override fun isFavorite(id: String): Observable<Boolean> {
        return dataManager.getObjectFromDb(Album::class.java, dataManager.getUserFavAlbumId())
                .map { it.photocards.find { it.id == id } != null }
                .ioToMain()
    }
}
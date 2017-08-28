package io.github.vladimirmi.photon.data.jobs.queue

import com.birbit.android.jobqueue.JobManager
import io.github.vladimirmi.photon.data.jobs.*
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.dto.UserDto
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.utils.JobStatus
import io.github.vladimirmi.photon.utils.observe
import io.reactivex.Observable

/**
 * Created by Vladimir Mikhalev 16.08.2017.
 */

class Jobs(private val dataManager: DataManager,
           private val jobManager: JobManager) {

    fun albumCreate(albumDto: AlbumDto) = jobStatusObservable(AlbumCreateJob.TAG) {
        val album = with(albumDto) {
            Album(owner = dataManager.getProfileId(),
                    title = title,
                    description = description)
        }
        val profile = getProfile().apply {
            albums.add(album)
            sync = false
        }
        dataManager.saveToDB(profile)
    }

    fun albumDelete(id: String) = jobStatusObservable(AlbumDeleteJob.TAG) {
        val album = getAlbum(id).apply {
            active = false
            sync = false
        }
        dataManager.saveToDB(album)
    }


    fun albumEdit(albumDto: AlbumDto) = jobStatusObservable(AlbumEditJob.TAG) {
        val album = getAlbum(albumDto.id).apply {
            title = albumDto.title
            description = albumDto.description
            sync = false
        }
        dataManager.saveToDB(album)
    }

    fun albumAddFavorite(id: String) = jobStatusObservable(AlbumAddFavoritePhotoJob.TAG) {
        val photocard = getPhotocard(id)
        val favAlbumId = dataManager.getUserFavAlbumId()
        val album = getAlbum(favAlbumId).apply {
            photocards.add(photocard)
            sync = false
        }
        dataManager.saveToDB(album)
    }


    fun albumDeleteFavorite(id: String) = jobStatusObservable(AlbumDeleteFavoritePhotoJob.TAG) {
        val favAlbumId = dataManager.getUserFavAlbumId()
        val album = getAlbum(favAlbumId).apply {
            photocards.removeAll { it.id == id }
            sync = false
        }
        dataManager.saveToDB(album)
    }

    fun photocardCreate(photocard: Photocard) = jobStatusObservable(PhotocardCreateJob.TAG) {
        val album = getAlbum(photocard.album)
        album.photocards.add(photocard.apply { sync = false })
        dataManager.saveToDB(album)
    }

    fun photocardDelete(id: String) = jobStatusObservable(PhotocardDeleteJob.TAG) {
        val photocard = getPhotocard(id).apply {
            active = false
            sync = false
        }
        dataManager.saveToDB(photocard)
    }

    fun photocardAddView(id: String) = jobStatusObservable(PhotocardAddViewJob.TAG) {
        val photocard = getPhotocard(id).apply {
            views++
            sync = false
        }
        dataManager.saveToDB(photocard)
    }


    fun profileEdit(userDto: UserDto) = jobStatusObservable(ProfileEditJob.TAG) {
        val profile = getProfile().apply {
            login = userDto.login
            name = userDto.name
            avatar = userDto.avatar
            sync = false
        }
        dataManager.saveToDB(profile)
    }

    private fun getAlbum(id: String) = dataManager.getDetachedObjFromDb(Album::class.java, id)!!
    private fun getPhotocard(id: String) = dataManager.getDetachedObjFromDb(Photocard::class.java, id)!!
    private fun getProfile() = dataManager.getDetachedObjFromDb(User::class.java, dataManager.getProfileId())!!

    private fun jobStatusObservable(tag: String, action: () -> Unit): Observable<JobStatus> =
            Observable.fromCallable(action).flatMap { jobManager.observe(tag) }
}

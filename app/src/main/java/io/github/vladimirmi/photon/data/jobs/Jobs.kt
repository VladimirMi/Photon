package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.JobManager
import io.github.vladimirmi.photon.data.jobs.album.*
import io.github.vladimirmi.photon.data.jobs.photocard.PhotocardAddViewJob
import io.github.vladimirmi.photon.data.jobs.photocard.PhotocardCreateJob
import io.github.vladimirmi.photon.data.jobs.photocard.PhotocardDeleteJob
import io.github.vladimirmi.photon.data.jobs.profile.ProfileEditJob
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.dto.UserDto
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.models.realm.Synchronizable
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.utils.JobStatus
import io.github.vladimirmi.photon.utils.observe
import io.reactivex.Observable
import io.realm.RealmObject

/**
 * Created by Vladimir Mikhalev 16.08.2017.
 */

class Jobs(private val dataManager: DataManager,
           private val jobManager: JobManager) {

    fun albumCreate(albumDto: AlbumDto) = jobStatusObservable(AlbumCreateJob.TAG) {
        val album = with(albumDto) {
            Album(owner = dataManager.getProfileId(),
                    title = title,
                    description = description,
                    sync = false)
        }
        val profile = getProfile()
        profile.albums.add(album)
        dataManager.save(profile)
    }

    fun albumDelete(id: String): Observable<JobStatus> {
        val album = getAlbum(id)
        return Observable.fromIterable(album.photocards)
                .flatMap { photocardDelete(it.id) }
                .ignoreElements()
                .andThen(jobStatusObservable(AlbumDeleteJob.TAG) {
                    album.active = false
                    save(album)
                })
    }


    fun albumEdit(albumDto: AlbumDto) = jobStatusObservable(AlbumEditJob.TAG) {
        val album = getAlbum(albumDto.id).apply {
            title = albumDto.title
            description = albumDto.description
            sync = false
        }
        save(album)
    }

    fun albumAddFavorite(id: String) = jobStatusObservable(AlbumAddFavoritePhotoJob.TAG) {
        val photocard = getPhotocard(id)
        val favAlbumId = dataManager.getUserFavAlbumId()
        val album = getAlbum(favAlbumId).apply {
            photocards.add(photocard)
            sync = false
        }
        save(album)
    }


    fun albumDeleteFavorite(id: String) = jobStatusObservable(AlbumDeleteFavoritePhotoJob.TAG) {
        val favAlbumId = dataManager.getUserFavAlbumId()
        val album = getAlbum(favAlbumId)
        album.photocards.removeAll { it.id == id }
        save(album)
    }

    fun photocardCreate(photocard: Photocard) = jobStatusObservable(PhotocardCreateJob.TAG) {
        photocard.sync = false
        val album = getAlbum(photocard.album)
        album.photocards.add(photocard)
        dataManager.save(album)
    }

    fun photocardDelete(id: String) = jobStatusObservable(PhotocardDeleteJob.TAG) {
        val photocard = getPhotocard(id)
        photocard.active = false
        save(photocard)
    }

    fun photocardAddView(id: String) = jobStatusObservable(PhotocardAddViewJob.TAG) {
        val photocard = getPhotocard(id)
        photocard.views++
        save(photocard)
    }


    fun profileEdit(userDto: UserDto) = jobStatusObservable(ProfileEditJob.TAG) {
        val profile = getProfile()
        profile.login = userDto.login
        profile.name = userDto.name
        profile.avatar = userDto.avatar

        save(profile)
    }

    private fun getAlbum(id: String) = dataManager.getDetachedObjFromDb(Album::class.java, id)!!
    private fun getPhotocard(id: String) = dataManager.getDetachedObjFromDb(Photocard::class.java, id)!!
    private fun getProfile() = dataManager.getDetachedObjFromDb(User::class.java, dataManager.getProfileId())!!

    private fun jobStatusObservable(tag: String, action: () -> Unit): Observable<JobStatus> =
            Observable.fromCallable(action).flatMap { jobManager.observe(tag) }

    private fun save(obj: Synchronizable) {
        obj.sync = false
        dataManager.save(obj as RealmObject)
    }
}

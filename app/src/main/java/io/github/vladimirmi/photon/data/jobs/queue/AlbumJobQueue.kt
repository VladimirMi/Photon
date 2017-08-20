package io.github.vladimirmi.photon.data.jobs.queue

import io.github.vladimirmi.photon.data.jobs.AlbumCreateJob
import io.github.vladimirmi.photon.data.jobs.AlbumDeleteJob
import io.github.vladimirmi.photon.data.jobs.AlbumEditJob
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.data.models.req.AlbumEditReq
import io.github.vladimirmi.photon.data.models.req.NewAlbumReq
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.JobStatus
import io.reactivex.Observable

/**
 * Created by Vladimir Mikhalev 25.07.2017.
 */

class AlbumJobQueue(private val jobQueue: JobQueue,
                    private val dataManager: DataManager) {

    fun queueCreateJob(request: NewAlbumReq): Observable<JobStatus> {
        val profile = dataManager.getDetachedObjFromDb(User::class.java, dataManager.getProfileId())!!
        val album = Album(id = request.id, owner = request.owner,
                title = request.title, description = request.description)
        profile.albums.add(album)
        dataManager.saveToDB(profile)

        return jobQueue.add(AlbumCreateJob(request))
    }

    fun queueDeleteJob(id: String): Observable<JobStatus> {
        val cache = DaggerService.appComponent.cache()
        cache.removeAlbum(id)
        dataManager.removeFromDb(Album::class.java, id)

        return jobQueue.add(AlbumDeleteJob(id))
    }

    fun queueEditJob(request: AlbumEditReq): Observable<JobStatus> {
        val album = dataManager.getDetachedObjFromDb(Album::class.java, request.id)!!.apply {
            title = request.title
            description = request.description
        }
        dataManager.saveToDB(album)

        return jobQueue.add(AlbumEditJob(request))
    }
}

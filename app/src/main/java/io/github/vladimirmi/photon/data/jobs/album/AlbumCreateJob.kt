package io.github.vladimirmi.photon.data.jobs.album

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.data.models.req.AlbumNewReq
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.JobGroup
import io.github.vladimirmi.photon.utils.JobPriority
import io.github.vladimirmi.photon.utils.cancelOrWaitConnection
import io.github.vladimirmi.photon.utils.logCancel

/**
 * Created by Vladimir Mikhalev 17.07.2017.
 */

class AlbumCreateJob(private val albumId: String)
    : Job(Params(JobPriority.HIGH)
        .setGroupId(JobGroup.ALBUM)
        .addTags(TAG)
        .requireNetwork()
        .persist()) {

    companion object {
        const val TAG = "AlbumCreateJob"
    }

    override fun onAdded() {}

    override fun onRun() {
        val dataManager = DaggerService.appComponent.dataManager()
        var error: Throwable? = null

        val album = dataManager.getDetachedObjFromDb(Album::class.java, albumId)!!
        val request = AlbumNewReq.fromAlbum(album)

        dataManager.createAlbum(request)
                .doOnNext {
                    val profile = dataManager.getDetachedObjFromDb(User::class.java, dataManager.getProfileId())!!
                    profile.albums.add(it)
                    dataManager.save(profile)
                    album.photocards.forEach { photocard ->
                        dataManager.save(photocard.apply {
                            this.album = it.id
                            sync = false
                        })
                    }

                    deleteTempAlbum()
                }
                .blockingSubscribe({}, { error = it })

        error?.let { throw it }
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        logCancel(cancelReason, throwable)
    }

    private fun deleteTempAlbum() {
        DaggerService.appComponent.dataManager().removeFromDb(Album::class.java, albumId)
        DaggerService.appComponent.cache().removeAlbum(albumId)
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int) =
            cancelOrWaitConnection(throwable, runCount)
}
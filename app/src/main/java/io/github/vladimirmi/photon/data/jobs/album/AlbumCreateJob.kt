package io.github.vladimirmi.photon.data.jobs.album

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import io.github.vladimirmi.photon.data.managers.extensions.JobPriority
import io.github.vladimirmi.photon.data.managers.extensions.cancelOrWaitConnection
import io.github.vladimirmi.photon.data.managers.extensions.getAlbum
import io.github.vladimirmi.photon.data.managers.extensions.logCancel
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.req.AlbumNewReq
import io.github.vladimirmi.photon.di.DaggerService

/**
 * Created by Vladimir Mikhalev 17.07.2017.
 */

class AlbumCreateJob(private val albumId: String)
    : Job(Params(JobPriority.HIGH)
        .addTags(TAG + albumId)
        .requireNetwork()) {

    companion object {
        const val TAG = "AlbumCreateJob"
    }

    private val dataManager = DaggerService.appComponent.dataManager()
    private val cache = DaggerService.appComponent.cache()

    override fun onAdded() {}

    override fun onRun() {
        val album = dataManager.getAlbum(albumId)
        val request = AlbumNewReq.fromAlbum(album)

        val albumRes = dataManager.createAlbum(request).blockingGet()

        album.id = albumRes.id
        dataManager.save(album)
        dataManager.removeFromDb(Album::class.java, albumId)
        cache.removeAlbum(albumId)
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        logCancel(cancelReason, throwable)
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int) =
            cancelOrWaitConnection(throwable, runCount)
}
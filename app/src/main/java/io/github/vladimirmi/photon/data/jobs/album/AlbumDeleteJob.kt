package io.github.vladimirmi.photon.data.jobs.album

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import io.github.vladimirmi.photon.data.managers.extensions.JobPriority
import io.github.vladimirmi.photon.data.managers.extensions.cancelOrWaitConnection
import io.github.vladimirmi.photon.data.managers.extensions.getAlbum
import io.github.vladimirmi.photon.data.managers.extensions.logCancel
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.di.DaggerService

/**
 * Created by Vladimir Mikhalev 18.07.2017.
 */


class AlbumDeleteJob(private val albumId: String)
    : Job(Params(JobPriority.HIGH)
        .addTags(TAG + albumId)
        .requireNetwork()) {

    companion object {
        const val TAG = "AlbumDeleteJob"
    }

    private val dataManager = DaggerService.appComponent.dataManager()
    private val cache = DaggerService.appComponent.cache()

    override fun onAdded() {}

    override fun onRun() {
        dataManager.deleteAlbum(albumId).blockingGet()
        dataManager.removeFromDb(Album::class.java, albumId)
        cache.removeAlbum(albumId)
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        logCancel(cancelReason, throwable)
        rollback()
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int) =
            cancelOrWaitConnection(throwable, runCount)

    private fun rollback() {
        val album = dataManager.getAlbum(albumId)
        album.active = true
        dataManager.save(album)
    }
}

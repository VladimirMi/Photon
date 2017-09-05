package io.github.vladimirmi.photon.data.jobs.album

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import io.github.vladimirmi.photon.data.managers.extensions.JobPriority
import io.github.vladimirmi.photon.data.managers.extensions.cancelOrWaitConnection
import io.github.vladimirmi.photon.data.managers.extensions.logCancel
import io.github.vladimirmi.photon.data.models.req.AlbumNewReq
import io.github.vladimirmi.photon.data.repository.album.AlbumJobRepository

/**
 * Created by Vladimir Mikhalev 17.07.2017.
 */

class AlbumCreateJob(private val albumId: String,
                     private val repository: AlbumJobRepository)
    : Job(Params(JobPriority.HIGH)
        .addTags(TAG + albumId)
        .requireNetwork()) {

    companion object {
        const val TAG = "AlbumCreateJob"
    }

    override fun onAdded() {}

    override fun onRun() {
        val album = repository.getAlbum(albumId)
        val request = AlbumNewReq.fromAlbum(album)

        val albumRes = repository.create(request).blockingGet()

        album.id = albumRes.id
        repository.save(album)
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        logCancel(cancelReason, throwable)
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int) =
            cancelOrWaitConnection(throwable, runCount)
}
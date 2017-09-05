package io.github.vladimirmi.photon.data.jobs.album

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import io.github.vladimirmi.photon.data.managers.extensions.JobPriority
import io.github.vladimirmi.photon.data.managers.extensions.cancelOrWaitConnection
import io.github.vladimirmi.photon.data.managers.extensions.logCancel
import io.github.vladimirmi.photon.data.repository.album.AlbumJobRepository

/**
 * Created by Vladimir Mikhalev 18.07.2017.
 */


class AlbumDeleteJob(private val albumId: String,
                     private val repository: AlbumJobRepository)
    : Job(Params(JobPriority.HIGH)
        .addTags(TAG + albumId)
        .requireNetwork()) {

    companion object {
        const val TAG = "AlbumDeleteJob"
    }

    override fun onAdded() {}

    override fun onRun() {
        repository.delete(albumId).blockingGet()
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        logCancel(cancelReason, throwable)
        repository.rollbackDelete(albumId)
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int) =
            cancelOrWaitConnection(throwable, runCount)
}

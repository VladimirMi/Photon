package io.github.vladimirmi.photon.data.jobs.album

import io.github.vladimirmi.photon.data.jobs.ChainJob
import io.github.vladimirmi.photon.data.managers.extensions.JobGroup
import io.github.vladimirmi.photon.di.DaggerService

/**
 * Created by Vladimir Mikhalev 18.07.2017.
 */


class AlbumDeleteJob(private val albumId: String)
    : ChainJob(TAG, JobGroup.ALBUM, albumId) {

    companion object {
        const val TAG = "AlbumDeleteJob"
    }

    override val needCancel = groupTag

    override fun execute() {
        val repository = DaggerService.appComponent.albumJobRepository()
        repository.delete(albumId).blockingGet()
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        super.onCancel(cancelReason, throwable)
        val repository = DaggerService.appComponent.albumJobRepository()
        repository.rollbackDelete(albumId)
    }

    override fun copy() = throw UnsupportedOperationException()
}

package io.github.vladimirmi.photon.data.jobs.photocard

import io.github.vladimirmi.photon.data.jobs.ChainJob
import io.github.vladimirmi.photon.data.jobs.album.AlbumCreateJob
import io.github.vladimirmi.photon.data.managers.extensions.JobGroup
import io.github.vladimirmi.photon.di.DaggerService

/**
 * Created by Vladimir Mikhalev 21.07.2017.
 */

class PhotocardAddViewJob(private val photocardId: String)
    : ChainJob(TAG, JobGroup.PHOTOCARD, photocardId) {

    companion object {
        const val TAG = "PhotocardAddViewJob"
    }

    private val albumId = DaggerService.appComponent.photocardJobRepository().getPhotocard(photocardId).album
    override val needCreate = listOf(PhotocardCreateJob.TAG + photocardId,
            AlbumCreateJob.TAG + albumId)

    override fun execute() {
        val repository = DaggerService.appComponent.photocardJobRepository()
        repository.addView(result ?: photocardId).blockingGet()
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        super.onCancel(cancelReason, throwable)
        val repository = DaggerService.appComponent.photocardJobRepository()
        repository.rollbackAddView(photocardId)
    }

    override fun copy() = throw UnsupportedOperationException()
}
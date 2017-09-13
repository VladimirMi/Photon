package io.github.vladimirmi.photon.data.jobs.album

import io.github.vladimirmi.photon.data.jobs.ChainJob
import io.github.vladimirmi.photon.data.managers.extensions.cancelOrWaitConnection
import io.github.vladimirmi.photon.data.managers.extensions.logCancel
import io.github.vladimirmi.photon.data.models.req.AlbumEditReq
import io.github.vladimirmi.photon.data.repository.album.AlbumJobRepository
import io.github.vladimirmi.photon.utils.ErrorSingleObserver
import io.reactivex.schedulers.Schedulers

/**
 * Created by Vladimir Mikhalev 20.07.2017.
 */

class AlbumEditJob(private val albumId: String,
                   private val repository: AlbumJobRepository)
    : ChainJob(TAG, albumId) {

    companion object {
        const val TAG = "AlbumEditJob"
    }

    override val needCreate = AlbumCreateJob.TAG + albumId
    override val needReplace = TAG + albumId

    override fun onAdded() {}

    override fun onRun() {
        val album = repository.getAlbum(albumId)
        repository.edit(AlbumEditReq.from(album)).blockingGet()
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        logCancel(cancelReason, throwable)
        rollback()
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int) =
            cancelOrWaitConnection(throwable, runCount)


    private fun rollback() {
        repository.getAlbumFromNet(id)
                .doOnSuccess { repository.rollbackEdit(AlbumEditReq.from(it)) }
                .subscribeOn(Schedulers.io())
                .subscribeWith(ErrorSingleObserver())
    }
}
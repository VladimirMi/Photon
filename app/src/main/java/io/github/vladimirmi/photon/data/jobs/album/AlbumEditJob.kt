package io.github.vladimirmi.photon.data.jobs.album

import io.github.vladimirmi.photon.data.jobs.ChainJob
import io.github.vladimirmi.photon.data.managers.utils.JobGroup
import io.github.vladimirmi.photon.data.managers.utils.cancelOrWaitConnection
import io.github.vladimirmi.photon.data.models.req.AlbumEditReq
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.ErrorSingleObserver
import io.reactivex.schedulers.Schedulers

/**
 * Created by Vladimir Mikhalev 20.07.2017.
 */

class AlbumEditJob(private val albumId: String)
    : ChainJob(TAG, JobGroup.ALBUM, albumId) {

    companion object {
        const val TAG = "AlbumEditJob"
    }

    override val needCreate = listOf(AlbumCreateJob.TAG + albumId)
    override val needReplace = tag

    override fun onAdded() {}

    override fun onStart() {
        val repository = DaggerService.appComponent.albumJobRepository()
        val album = repository.getAlbum(result ?: albumId)
        repository.edit(AlbumEditReq.from(album)).blockingGet()
    }

    override fun onError(throwable: Throwable) {
        val repository = DaggerService.appComponent.albumJobRepository()
        repository.getAlbumFromNet(id)
                .doOnSuccess { repository.rollbackEdit(AlbumEditReq.from(it)) }
                .subscribeOn(Schedulers.io())
                .subscribeWith(ErrorSingleObserver())
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int) =
            cancelOrWaitConnection(throwable, runCount)


    private fun rollback() {

    }

    override fun copy() = throw UnsupportedOperationException()
}
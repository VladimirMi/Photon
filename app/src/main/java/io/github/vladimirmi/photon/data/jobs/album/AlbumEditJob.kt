package io.github.vladimirmi.photon.data.jobs.album

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import io.github.vladimirmi.photon.data.managers.extensions.JobPriority
import io.github.vladimirmi.photon.data.managers.extensions.cancelOrWaitConnection
import io.github.vladimirmi.photon.data.managers.extensions.getAlbum
import io.github.vladimirmi.photon.data.managers.extensions.logCancel
import io.github.vladimirmi.photon.data.models.realm.extensions.edit
import io.github.vladimirmi.photon.data.models.req.AlbumEditReq
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.ErrorObserver
import io.reactivex.schedulers.Schedulers

/**
 * Created by Vladimir Mikhalev 20.07.2017.
 */

class AlbumEditJob(albumId: String)
    : Job(Params(JobPriority.HIGH)
        .addTags(TAG + albumId)
        .requireNetwork()) {

    companion object {
        const val TAG = "AlbumEditJob"
    }

    private val dataManager = DaggerService.appComponent.dataManager()
    private val album = dataManager.getAlbum(albumId)

    override fun onAdded() {}

    override fun onRun() {
        val request = AlbumEditReq.from(album)
        dataManager.editAlbum(request).blockingGet()
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        logCancel(cancelReason, throwable)
        rollback()
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int) =
            cancelOrWaitConnection(throwable, runCount)


    private fun rollback() {
        dataManager.getAlbumFromNet(id, force = true)
                .doOnNext { album.edit(AlbumEditReq.from(it)) }
                .subscribeOn(Schedulers.io())
                .subscribeWith(ErrorObserver())
    }
}
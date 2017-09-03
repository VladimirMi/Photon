package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.JobManager
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.managers.extensions.EmptyJobCallback
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.models.realm.Synchronizable
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.utils.Query
import io.github.vladimirmi.photon.utils.RealmOperator
import io.reactivex.Completable
import io.reactivex.Observable
import timber.log.Timber
import java.util.*
import kotlin.concurrent.schedule

/**
 * Created by Vladimir Mikhalev 30.08.2017.
 */

class JobsManager(dataManager: DataManager,
                  private val jobManager: JobManager) {

    var syncComplete = true
        private set
    private var isRunning = false

    private val profileManager = ProfileJobsManager(dataManager)
    private val albumManager = AlbumJobsManager(dataManager)
    private val photocardManager = PhotocardJobsManager(dataManager)

    //todo отдельный метод в realm manager
    private val query = listOf(Query("sync", RealmOperator.EQUALTO, false))
    private val profile = dataManager.search(User::class.java, query, detach = true)
    private val albums = dataManager.search(Album::class.java, query, detach = true)
    private val photocards = dataManager.search(Photocard::class.java, query, detach = true)

    fun subscribe(): Completable {
        return Observable.merge(profile, albums, photocards)
                .flatMapIterable { it }
                .cast(Synchronizable::class.java)
                .doOnNext {
                    Timber.e("sync = ${it.sync}")
                    syncing(true)
                    nextJob(it)
                }
                .ignoreElements()
                .doOnSubscribe { jobManager.addCallback(jobCallback) }
                .doFinally { jobManager.removeCallback(jobCallback) }
    }

    private val jobCallback = object : EmptyJobCallback() {
        override fun onDone(job: Job) {
            job.tags?.firstOrNull()?.let { completeJob(it) }
        }
    }

    private fun nextJob(it: Synchronizable) {
        if (isRunning) return
        val job = when (it) {
            is User -> profileManager.nextJob(it)
            is Album -> albumManager.nextJob(it)
            is Photocard -> photocardManager.nextJob(it)
            else -> null
        }
        job?.let {
            Timber.e("nextJob: $it")
            isRunning = true
            jobManager.addJobInBackground(it)
        }
    }

    private fun completeJob(tag: String) {
        isRunning = false
        syncing(false)
        profileManager.completeJob(tag)
        albumManager.completeJob(tag)
        photocardManager.completeJob(tag)
    }

    private val syncTimer = Timer()
    private var syncTask: TimerTask? = null
    private fun syncing(syncing: Boolean) {
        if (syncing) {
            syncComplete = false
            syncTask?.cancel()
        } else {
            syncTask = syncTimer.schedule(10000) { syncComplete = true }
        }
    }
}
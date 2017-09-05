package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.JobManager
import io.github.vladimirmi.photon.core.App
import io.github.vladimirmi.photon.data.managers.extensions.EmptyJobCallback
import io.github.vladimirmi.photon.data.managers.extensions.JobStatus
import io.github.vladimirmi.photon.data.managers.extensions.observe
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.models.realm.Synchronizable
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.data.repository.album.AlbumJobRepository
import io.github.vladimirmi.photon.data.repository.photocard.PhotocardJobRepository
import io.github.vladimirmi.photon.data.repository.profile.ProfileJobRepository
import io.github.vladimirmi.photon.di.DaggerScope
import io.reactivex.Completable
import io.reactivex.Observable
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule

/**
 * Created by Vladimir Mikhalev 30.08.2017.
 */

@DaggerScope(App::class)
class JobsManager
@Inject constructor(private val jobManager: JobManager,
                    profileJobRepository: ProfileJobRepository,
                    albumJobRepository: AlbumJobRepository,
                    photocardJobRepository: PhotocardJobRepository) {

    //todo спрашивать синхронизацию по каждому объекту
    var syncComplete = true
        private set
    private var isRunning = false

    private val profileManager = ProfileJobsManager(profileJobRepository)
    private val albumManager = AlbumJobsManager(albumJobRepository)
    private val photocardManager = PhotocardJobsManager(photocardJobRepository)

    private val profile = profileJobRepository.getNotSync()
    private val albums = albumJobRepository.getNotSync()
    private val photocards = photocardJobRepository.getNotSync()

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

    fun observe(tag: String): Observable<JobStatus> = jobManager.observe(tag)
}
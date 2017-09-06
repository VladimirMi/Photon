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
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.08.2017.
 */

@DaggerScope(App::class)
class JobsManager
@Inject constructor(private val jobManager: JobManager,
                    profileJobRepository: ProfileJobRepository,
                    albumJobRepository: AlbumJobRepository,
                    photocardJobRepository: PhotocardJobRepository) {

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
                .doOnNext { syncQueue.put(it.id, it) }
                .doOnSubscribe { jobManager.addCallback(jobCallback) }
                .doFinally { jobManager.removeCallback(jobCallback) }
                .ignoreElements()
                .subscribeOn(Schedulers.io())
    }

    fun isSync(id: String) = !syncQueue.contains(id)

    fun syncComplete() = syncQueue.isEmpty()

    fun observe(tag: String): Observable<JobStatus> = jobManager.observe(tag)

    private val jobCallback = object : EmptyJobCallback() {
        override fun onDone(job: Job) {
            if (job is SyncCompleteJob<*>) {
                syncQueue.remove(job.tags?.firstOrNull()?.removePrefix(SyncCompleteJob.TAG))
            }
        }
    }

    private val syncQueue = object : HashMap<String, Synchronizable>() {
        override fun put(key: String, value: Synchronizable): Synchronizable? {
            val put = super.put(key, value)
            if (!isRunning) synchronize()
            return put
        }

        override fun remove(key: String): Synchronizable? {
            val remove = super.remove(key)
            synchronize()
            return remove
        }
    }

    private fun synchronize() {
        syncQueue.values.forEach {
            if (runNextJob(it)) {
                isRunning = true
                return
            }
        }
        isRunning = false
    }

    private fun runNextJob(it: Synchronizable): Boolean {
        val job = when (it) {
            is User -> profileManager.nextJob(it)
            is Album -> albumManager.nextJob(it)
            is Photocard -> photocardManager.nextJob(it)
            else -> null
        }
        job?.let {
            Timber.e("nextJob: $it")
            jobManager.addJobInBackground(it)
            return true
        }
        return false
    }
}
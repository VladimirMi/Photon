package io.github.vladimirmi.photon.data.managers

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.JobManager
import io.github.vladimirmi.photon.data.jobs.*
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.models.realm.Synchronizable
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.data.models.req.AlbumEditReq
import io.github.vladimirmi.photon.data.models.req.AlbumNewReq
import io.github.vladimirmi.photon.data.models.req.PhotocardNewReq
import io.github.vladimirmi.photon.data.models.req.ProfileEditReq
import io.github.vladimirmi.photon.utils.Query
import io.github.vladimirmi.photon.utils.RealmOperator
import io.github.vladimirmi.photon.utils.addAndObserve
import io.reactivex.Completable
import io.reactivex.Observable
import io.realm.RealmObject
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by Vladimir Mikhalev 25.08.2017.
 */

class RealmSynchronizer(private val dataManager: DataManager,
                        private val jobManager: JobManager) {

    fun syncAll(): Completable {
        val query = listOf(Query("sync", RealmOperator.EQUALTO, false))

        return Observable.timer(15, TimeUnit.SECONDS)
                .flatMap {
                    Observable.merge(dataManager.search(Album::class.java, query),
                            dataManager.search(Photocard::class.java, query),
                            dataManager.search(User::class.java, query))
                }
                .flatMap { Observable.fromIterable(it) }
                .flatMapCompletable { obj ->
                    obj as Synchronizable
                    if (obj.active) {
                        if (obj.isTemp()) create(obj) else edit(obj)
                    } else {
                        delete(obj)
                    }
                }
    }

    private fun create(obj: Synchronizable): Completable {
        Timber.e("create: ${obj.id}")
        val job: Job? = when (obj) {
            is Album -> AlbumCreateJob(AlbumNewReq(obj))
            is Photocard -> if (obj.canCreate()) PhotocardCreateJob(PhotocardNewReq(obj)) else null
            else -> throw IllegalStateException()
        }
        return job?.run {
            jobManager.addAndObserve(this)
                    .ignoreElements()
                    .doOnComplete { saveSync(obj) }
        } ?: Completable.complete()
    }

    private fun delete(obj: Synchronizable): Completable {
        Timber.e("delete: ${obj.id}")
        val job: Job = when (obj) {
            is Album -> AlbumDeleteJob(obj.id)
            is Photocard -> PhotocardDeleteJob(obj.id)
            else -> throw IllegalStateException()
        }
        return jobManager.addAndObserve(job)
                .ignoreElements()
                .doOnComplete { saveSync(obj) }
    }

    private fun edit(obj: Synchronizable): Completable {
        Timber.e("create: ${obj.id}")
        return when (obj) {
            is Album -> editAlbum(obj)
            is Photocard -> editPhotocard(obj)
            is User -> editUser(obj)
            else -> throw IllegalStateException()
        }
    }


    private fun editAlbum(localAlbum: Album): Completable {
        val jobs: List<Job> = if (localAlbum.isFavorite) {
            dataManager.getAlbumFromNet(localAlbum.id)
                    .map { album ->
                        if (localAlbum.photocards.size > album.photocards.size) {
                            localAlbum.photocards.removeAll(album.photocards)
                            localAlbum.photocards.map { AlbumAddFavoritePhotoJob(it.id) as Job }
                        } else if (localAlbum.photocards.size < album.photocards.size) {
                            album.photocards.removeAll(localAlbum.photocards)
                            album.photocards.map { AlbumDeleteFavoritePhotoJob(it.id) as Job }
                        } else {
                            emptyList()
                        }
                    }
                    .blockingFirst()
        } else {
            listOf(AlbumEditJob(AlbumEditReq(localAlbum)))
        }
        return Observable.fromIterable(jobs)
                .flatMap { jobManager.addAndObserve(it) }
                .ignoreElements()
                .doOnComplete { saveSync(localAlbum) }
    }

    private fun editPhotocard(photocard: Photocard): Completable {
        return jobManager.addAndObserve(PhotocardAddViewJob(photocard.id))
                .ignoreElements()
                .doOnComplete { saveSync(photocard) }
    }

    private fun editUser(user: User): Completable {
        return jobManager.addAndObserve(ProfileEditJob(ProfileEditReq(user)))
                .ignoreElements()
                .doOnComplete { saveSync(user) }
    }

    private fun saveSync(it: Synchronizable) {
        dataManager.saveToDB(it.apply { sync = true } as RealmObject)
    }
}

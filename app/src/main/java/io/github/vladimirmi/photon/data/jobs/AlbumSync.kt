package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.JobManager
import io.github.vladimirmi.photon.data.jobs.album.*
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.realm.Album
import timber.log.Timber

/**
 * Created by Vladimir Mikhalev 29.08.2017.
 */

class AlbumSync(private val jobManager: JobManager,
                private val dataManager: DataManager) {

    fun create(localAlbum: Album) {
        Timber.e("create: ")
        saveSync(localAlbum)
        jobManager.addJobInBackground(AlbumCreateJob(localAlbum.id))
    }

    fun delete(localAlbum: Album) {
        Timber.e("delete: ")
        saveSync(localAlbum)
        jobManager.addJobInBackground(AlbumDeleteJob(localAlbum.id))
    }

    fun edit(localAlbum: Album) {
        Timber.e("edit: ")
        saveSync(localAlbum)

        val jobs: List<Job> = if (localAlbum.isFavorite) {
            getEditJobsForFavoriteAlbum(localAlbum)
        } else {
            listOf(AlbumEditJob(localAlbum.id))
        }

        Timber.e("edit: size ${jobs.size}")

        jobs.forEach { jobManager.addJobInBackground(it) }
    }

    private fun getEditJobsForFavoriteAlbum(local: Album): List<Job> {
        val net = dataManager.getAlbumFromNet(local.id, "0").blockingFirst()

        val localCards = local.photocards.map { it.id }
        val netCards = net.photocards.filter { it.active }.map { it.id }

        return when {
            localCards.size > netCards.size -> {
                (localCards - netCards).map { AlbumAddFavoritePhotoJob(it) as Job }
            }
            localCards.size < netCards.size -> {
                (netCards - localCards).map { AlbumDeleteFavoritePhotoJob(it) as Job }
            }
            else -> emptyList()
        }
    }

    private fun saveSync(localAlbum: Album) {
        localAlbum.sync = true
        dataManager.save(localAlbum)
    }
}
package io.github.vladimirmi.photon.data.managers

import com.birbit.android.jobqueue.JobManager
import io.github.vladimirmi.photon.data.jobs.AlbumSync
import io.github.vladimirmi.photon.data.jobs.PhotocardSync
import io.github.vladimirmi.photon.data.jobs.ProfileSync
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.models.realm.Synchronizable
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.utils.Query
import io.github.vladimirmi.photon.utils.RealmOperator
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

/**
 * Created by Vladimir Mikhalev 25.08.2017.
 */

class RealmSynchronizer(jobManager: JobManager,
                        private val dataManager: DataManager) {

    private val albumSync = AlbumSync(jobManager, dataManager)
    private val photocardSync = PhotocardSync(jobManager, dataManager)
    private val profileSync = ProfileSync(jobManager, dataManager)


    fun syncAll(): Completable {
        val query = listOf(Query("sync", RealmOperator.EQUALTO, false))

        return dataManager.isNetworkAvailable()
                .filter { it }
                .flatMap {
                    Observable.merge(dataManager.search(Album::class.java, query, detach = true),
                            dataManager.search(Photocard::class.java, query, detach = true),
                            dataManager.search(User::class.java, query, detach = true))
                }
                .filter { it.isNotEmpty() }
                .flatMap { Observable.fromIterable(it) }
                .withLatestFrom(dataManager.isNetworkAvailable(),
                        BiFunction { obj: Any, netAvail: Boolean ->
                            if (netAvail) obj else netAvail
                        })
                .filter { it !is Boolean }
                .doOnNext { obj ->
                    obj as Synchronizable
                    if (obj.active) {
                        if (obj.isTemp()) create(obj) else edit(obj)
                    } else {
                        delete(obj)
                    }
                }
                .ignoreElements()
    }

    private fun create(obj: Synchronizable) {
        when (obj) {
            is Album -> albumSync.create(obj)
            is Photocard -> photocardSync.create(obj)
            else -> throw IllegalStateException()
        }
    }

    private fun delete(obj: Synchronizable) {
        when (obj) {
            is Album -> albumSync.delete(obj)
            is Photocard -> photocardSync.delete(obj)
            else -> throw IllegalStateException()
        }
    }

    private fun edit(obj: Synchronizable) {
        when (obj) {
            is Album -> albumSync.edit(obj)
            is Photocard -> photocardSync.edit(obj)
            is User -> profileSync.edit(obj)
            else -> throw IllegalStateException()
        }
    }
}

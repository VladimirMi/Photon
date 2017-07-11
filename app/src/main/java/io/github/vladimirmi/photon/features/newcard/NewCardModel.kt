package io.github.vladimirmi.photon.features.newcard

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.JobManager
import io.github.vladimirmi.photon.data.jobs.EmptyJobCallback
import io.github.vladimirmi.photon.data.jobs.UploadPhotoJob
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.models.realm.Tag
import io.github.vladimirmi.photon.utils.Query
import io.github.vladimirmi.photon.utils.RealmOperator
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposables
import io.realm.Sort
import timber.log.Timber

class NewCardModel(val dataManager: DataManager, val jobManager: JobManager) : INewCardModel {
    override var photoCard = Photocard()

    override fun addFilter(filter: Pair<String, String>) {
        setFilterField(filter.first, filter.second)
    }

    override fun removeFilter(filter: Pair<String, String>) {
        setFilterField(filter.first, filter.second, remove = true)
    }

    fun setFilterField(name: String, value: String, remove: Boolean = false) {
        Timber.e("setFilterField: with $name to $value, remove = $remove")
        when (name) {
            "filters.dish" -> photoCard.filters.dish = if (remove) "" else value
            "filters.decor" -> photoCard.filters.decor = if (remove) "" else value
            "filters.light" -> photoCard.filters.light = if (remove) "" else value
            "filters.lightDirection" -> photoCard.filters.lightDirection = if (remove) "" else value
            "filters.lightSource" -> photoCard.filters.lightSource = if (remove) "" else value
            "filters.temperature" -> photoCard.filters.temperature = if (remove) "" else value
            "filters.nuances" -> {
                val values = photoCard.filters.nuances.split(", ").toMutableList()
                values.removeAll { it.isEmpty() }
                if (remove) values.remove(value) else values.add(value)
                photoCard.filters.nuances = values.joinToString()
            }
        }
    }

    override fun search(tag: String): Observable<List<Tag>> {
        val query = Query("value", RealmOperator.CONTAINS, tag)
        return dataManager.search(Tag::class.java, listOf(query), sortBy = "value")
                .map { if (it.size > 3) it.subList(0, 3) else it }
    }

    override fun addTag(tag: Tag) {
        if (!photoCard.tags.contains(tag)) {
            photoCard.tags.add(tag)
        }
    }

    override fun getAlbums(): Observable<List<Album>> {
        val query = Query("owner", RealmOperator.EQUALTO, dataManager.getProfileId())
        return dataManager.search(Album::class.java, listOf(query), sortBy = "views", order = Sort.DESCENDING)
    }

    override fun savePhotoUri(uri: String) {
        Timber.e("savePhotoUri: $uri")
        photoCard.photo = uri
    }

    override fun uploadPhotocard(): Single<Unit> {
        photoCard.withId()
        photoCard.owner = dataManager.getProfileId()
        val uploadJob = UploadPhotoJob(photoCard)
        jobManager.addJobInBackground(uploadJob)

        return Single.create { e ->
            val callback = object : EmptyJobCallback() {
                override fun onDone(job: Job) {
                    if (!e.isDisposed && uploadJob.id == job.id) e.onSuccess(Unit)
                }

                override fun onJobCancelled(job: Job, byCancelRequest: Boolean, throwable: Throwable?) {
                    if (!e.isDisposed && throwable != null && uploadJob.id == job.id) e.onError(throwable)
                }
            }

            jobManager.addCallback(callback)
            e.setDisposable(Disposables.fromRunnable { jobManager.removeCallback(callback) })
        }

    }
}
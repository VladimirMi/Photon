package io.github.vladimirmi.photon.features.newcard

import com.birbit.android.jobqueue.JobManager
import io.github.vladimirmi.photon.data.jobs.CreatePhotoJob
import io.github.vladimirmi.photon.data.jobs.singleResultFor
import io.github.vladimirmi.photon.data.managers.Cache
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.models.realm.Tag
import io.github.vladimirmi.photon.utils.Query
import io.github.vladimirmi.photon.utils.RealmOperator
import io.github.vladimirmi.photon.utils.ioToMain
import io.reactivex.Observable
import io.reactivex.Single
import io.realm.Sort
import timber.log.Timber

class NewCardModel(val dataManager: DataManager, val jobManager: JobManager, val cache: Cache) : INewCardModel {
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

    override fun search(tag: String): Observable<List<String>> {
        val query = Query("value", RealmOperator.CONTAINS, tag)
        val tags = dataManager.search(Tag::class.java, listOf(query), sortBy = "value")
                .map { cache.cacheTags(it) }
                .map { cache.tags }

        return Observable.merge(Observable.just(cache.tags), tags)
                .map { if (it.size > 3) it.subList(0, 3) else it }
                .ioToMain()
    }

    override fun addTag(tag: String) {
        if (photoCard.tags.count { it.value == tag } == 0) {
            photoCard.tags.add(Tag(tag))
        }
    }

    override fun getAlbums(): Observable<List<AlbumDto>> {
        val query = Query("owner", RealmOperator.EQUALTO, dataManager.getProfileId())
        val albums = dataManager.search(Album::class.java, listOf(query), sortBy = "views", order = Sort.DESCENDING)
                .map { cache.cacheAlbums(it) }
                .map { cache.albums }

        return Observable.merge(Observable.just(cache.albums), albums).ioToMain()
    }

    override fun savePhotoUri(uri: String) {
        Timber.e("savePhotoUri: $uri")
        photoCard.photo = uri
    }

    override fun uploadPhotocard(): Single<Unit> {
        photoCard.withId()
        photoCard.owner = dataManager.getProfileId()
        val uploadJob = CreatePhotoJob(photoCard)
        jobManager.addJobInBackground(uploadJob)
        return jobManager.singleResultFor(uploadJob)
    }
}



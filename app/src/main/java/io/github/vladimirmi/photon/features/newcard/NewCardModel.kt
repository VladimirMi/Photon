package io.github.vladimirmi.photon.features.newcard

import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.data.jobs.queue.PhotocardJobQueue
import io.github.vladimirmi.photon.data.managers.Cache
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.models.realm.Tag
import io.github.vladimirmi.photon.utils.JobStatus
import io.github.vladimirmi.photon.utils.Query
import io.github.vladimirmi.photon.utils.RealmOperator
import io.github.vladimirmi.photon.utils.ioToMain
import io.reactivex.Observable
import io.realm.Sort
import timber.log.Timber

class NewCardModel(val dataManager: DataManager,
                   val photocardJobQueue: PhotocardJobQueue,
                   val cache: Cache) : INewCardModel {

    override var screenInfo = NewCardScreenInfo()

    override fun addFilter(filter: Pair<String, String>) {
        setFilterField(filter.first, filter.second)
    }

    override fun removeFilter(filter: Pair<String, String>) {
        setFilterField(filter.first, filter.second, remove = true)
    }

    private fun setFilterField(name: String, value: String, remove: Boolean = false) {
        Timber.e("setFilterField: with $name to $value, remove = $remove")
        when (name) {
            "filters.dish" -> screenInfo.filter.dish = if (remove) "" else value
            "filters.decor" -> screenInfo.filter.decor = if (remove) "" else value
            "filters.light" -> screenInfo.filter.light = if (remove) "" else value
            "filters.lightDirection" -> screenInfo.filter.lightDirection = if (remove) "" else value
            "filters.lightSource" -> screenInfo.filter.lightSource = if (remove) "" else value
            "filters.temperature" -> screenInfo.filter.temperature = if (remove) "" else value
            "filters.nuances" -> {
                val values = screenInfo.filter.nuances.split(", ").toMutableList()
                values.removeAll { it.isEmpty() }
                if (remove) values.remove(value) else values.add(value)
                screenInfo.filter.nuances = values.joinToString()
            }
        }
    }

    override fun searchTag(tag: String): Observable<List<String>> {
        val query = Query("value", RealmOperator.CONTAINS, tag)
        return dataManager.search(Tag::class.java, listOf(query), sortBy = "value")
                .map { cache.cacheTags(it) }
                .map { if (it.size > 3) it.subList(0, 3) else it }
                .ioToMain()
    }

    override fun addTag(tag: String) {
        if (!screenInfo.tags.contains(tag)) {
            screenInfo.tags.add(tag)
        }
    }

    override fun getAlbums(): Observable<List<AlbumDto>> {
        val query = Query("owner", RealmOperator.EQUALTO, dataManager.getProfileId())
        val albums = dataManager.search(Album::class.java, listOf(query), sortBy = "views", order = Sort.DESCENDING)
                .map { cache.cacheAlbums(it) }

        return Observable.merge(Observable.just(cache.albums), albums).ioToMain()
    }

    override fun getPageError(page: Page): Int? {
        return when (page) {
            Page.INFO -> if (screenInfo.title.isBlank()) R.string.newcard_err_title else null
            Page.PARAMS -> null
            Page.ALBUMS -> if (screenInfo.album.isBlank()) R.string.newcard_err_album else null
        }
    }

    override fun uploadPhotocard(): Observable<JobStatus> {
        val photoCard = Photocard().withId().apply {
            title = screenInfo.title
            tags.addAll(screenInfo.tags.map { Tag(it) })
            filters = screenInfo.filter
            owner = dataManager.getProfileId()
        }
        return Observable.just(dataManager.getDetachedObjFromDb(Album::class.java, screenInfo.album))
                .map { album ->
                    album.photocards.add(photoCard)
                    dataManager.saveToDB(album)
                }
                .flatMap { photocardJobQueue.queueCreateJob(photoCard.id, screenInfo.album) }
                .ioToMain()
    }
}



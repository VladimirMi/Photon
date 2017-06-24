package io.github.vladimirmi.photon.features.newcard

import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.managers.Query
import io.github.vladimirmi.photon.data.managers.RealmOperator
import io.github.vladimirmi.photon.data.models.Album
import io.github.vladimirmi.photon.data.models.Filter
import io.github.vladimirmi.photon.data.models.Photocard
import io.github.vladimirmi.photon.data.models.Tag
import io.reactivex.Observable
import io.realm.Sort
import timber.log.Timber

class NewCardModel(private val dataManager: DataManager) : INewCardModel {
    val filters = Filter()
    val photoCard = Photocard()

    override fun addFilter(filter: Pair<String, String>) {
        changeFilterField(filter.first, filter.second)
    }

    override fun removeFilter(filter: Pair<String, String>) {
        changeFilterField(filter.first, filter.second, remove = true)
    }

    fun changeFilterField(name: String, value: String, remove: Boolean = false) {
        Timber.e("changeFilterField: with $name to $value, remove = $remove")
        when (name) {
            "dish" -> filters.dish = if (remove) "" else value
            "decor" -> filters.decor = if (remove) "" else value
            "light" -> filters.light = if (remove) "" else value
            "lightDirection" -> filters.lightDirection = if (remove) "" else value
            "lightSource" -> filters.lightSource = if (remove) "" else value
            "temperature" -> filters.temperature = if (remove) "" else value
            "nuances" -> {
                val field = filters.nuances
                val mValue = if (field.isBlank()) value else ", $value"
                filters.nuances = if (remove) field.replace(mValue, "") else field + mValue
            }
        }
    }

    override fun search(tag: String): Observable<List<Tag>> {
        val query = Query("tag", RealmOperator.CONTAINS, tag)
        return dataManager.search(Tag::class.java, listOf(query), sortBy = "tag")
                .map { if (it.size > 3) it.subList(0, 3) else it }
    }

    override fun addTag(tag: Tag) {
        if (!photoCard.tags.contains(tag)) {
            photoCard.tags.add(tag)
        }
    }

    override fun getSavedTags() = photoCard.tags

    override fun getAlbums(): Observable<List<Album>> {
        val query = Query("owner", RealmOperator.EQUALTO, dataManager.getProfileId())
        return dataManager.search(Album::class.java, listOf(query), sortBy = "views", order = Sort.DESCENDING)
    }
}
package io.github.vladimirmi.photon.features.newcard

import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.data.managers.extensions.JobStatus
import io.github.vladimirmi.photon.data.mappers.AlbumCachingMapper
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.models.realm.Tag
import io.github.vladimirmi.photon.data.repository.photocard.PhotocardRepository
import io.github.vladimirmi.photon.data.repository.profile.ProfileRepository
import io.github.vladimirmi.photon.data.repository.recents.RecentsRepository
import io.github.vladimirmi.photon.utils.ioToMain
import io.reactivex.Observable
import io.realm.RealmList
import timber.log.Timber

class NewCardModel(private val profileRepository: ProfileRepository,
                   private val photocardRepository: PhotocardRepository,
                   private val recentsRepository: RecentsRepository,
                   private val albumMapper: AlbumCachingMapper) : INewCardModel {

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

    override fun searchTag(tag: String): Observable<List<String>> =
            recentsRepository.searchTag(tag)
                    .map { (if (it.size > 3) it.subList(0, 3) else it).map { it.value } }
                    .ioToMain()

    override fun addTag(tag: String) {
        if (!screenInfo.tags.contains(tag)) {
            Timber.e("addTag: $tag")
            screenInfo.tags.add(tag)
        }
    }

    override fun getAlbums(): Observable<List<AlbumDto>> =
            profileRepository.getAlbums()
                    .map { albumMapper.map(it) }
                    .ioToMain()

    override fun getPageError(page: Page) = when (page) {
        Page.INFO -> if (screenInfo.title.isBlank()) R.string.newcard_err_title else null
        Page.PARAMS -> null
        Page.ALBUMS -> if (screenInfo.album.isBlank()) R.string.newcard_err_album else null
    }

    override fun uploadPhotocard(): Observable<JobStatus> {
        val photocard = with(screenInfo) {
            Photocard(
                    title = title,
                    filters = filter,
                    photo = photo,
                    album = album,
                    owner = profileRepository.getProfileId(),
                    tags = RealmList<Tag>().apply { addAll(screenInfo.tags.map { Tag(it) }) }
            )
        }
        return photocardRepository.create(photocard)
                .ioToMain()
    }
}



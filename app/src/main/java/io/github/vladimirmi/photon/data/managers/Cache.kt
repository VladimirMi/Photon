package io.github.vladimirmi.photon.data.managers

import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.dto.PhotocardDto
import io.github.vladimirmi.photon.data.models.dto.UserDto
import io.github.vladimirmi.photon.data.models.realm.*
import java.util.TreeSet
import kotlin.collections.LinkedHashMap

/**
 * Created by Vladimir Mikhalev 17.07.2017.
 */

class Cache {
    private val photocardMap = LinkedHashMap<String, PhotocardDto>()
    val photocards: List<PhotocardDto> get() = photocardMap.values.toList()
    fun photocard(id: String) = photocardMap[id]

    fun cachePhotos(list: List<Photocard>) {
        list.forEach { if (it.active) photocardMap.put(it.id, PhotocardDto(it)) }
    }

    fun cachePhotocard(photocard: Photocard) {
        if (photocard.active) photocardMap.put(photocard.id, PhotocardDto(photocard))
    }


    private val albumMap = LinkedHashMap<String, AlbumDto>()
    val albums: List<AlbumDto> get() = albumMap.values.toList()
    fun album(id: String) = albumMap[id]

    fun cacheAlbums(list: List<Album>) {
        list.forEach { if (it.active) albumMap.put(it.id, AlbumDto(it)) }
    }

    fun cacheAlbum(album: Album) {
        if (album.active) albumMap.put(album.id, AlbumDto(album))
    }


    private val userMap = LinkedHashMap<String, UserDto>()
    val users: List<UserDto> get() = userMap.values.toList()
    fun user(id: String) = userMap[id]

    fun cacheUsers(list: List<User>) {
        list.forEach { if (it.active) userMap.put(it.id, UserDto(it)) }
    }

    fun cacheUser(user: User) {
        if (user.active) userMap.put(user.id, UserDto(user))
    }


    private val tagsSet = TreeSet<String>()
    val tags: List<String> get() = tagsSet.toList()

    fun cacheTags(list: List<Tag>) {
        list.forEach { tagsSet.add(it.value) }
    }

    fun cacheTag(tag: Tag) {
        tagsSet.add(tag.value)
    }


    private val searchSet = TreeSet<String>()
    val searches: List<String> get() = searchSet.toList()

    fun cacheSearches(list: List<Search>) {
        list.forEach { searchSet.add(it.value) }
    }

    fun cacheSearch(tag: Tag) {
        searchSet.add(tag.value)
    }
}
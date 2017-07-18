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

    fun removePhoto(id: String) {
        photocardMap.remove(id)
    }


    private val albumMap = LinkedHashMap<String, AlbumDto>()
    val albums: List<AlbumDto> get() = albumMap.values.toList()
    fun album(id: String) = albumMap[id]

    fun cacheAlbums(list: List<Album>) {
        list.forEach { cacheAlbum(it) }
    }
    fun cacheAlbum(album: Album) {
        if (album.active) albumMap.put(album.id, AlbumDto(album))
    }

    fun removeAlbum(id: String) {
        albumMap.remove(id)
    }


    private val userMap = LinkedHashMap<String, UserDto>()
    val users: List<UserDto> get() = userMap.values.toList()
    fun user(id: String) = userMap[id]

    fun cacheUsers(list: List<User>) {
        list.forEach { cacheUser(it) }
    }
    fun cacheUser(user: User) {
        if (user.active) userMap.put(user.id, UserDto(user))
    }

    fun removeUser(id: String) {
        userMap.remove(id)
    }


    private val tagsSet = TreeSet<String>()
    val tags: List<String> get() = tagsSet.toList()

    fun cacheTags(list: List<Tag>) {
        list.forEach { cacheTag(it) }
    }
    fun cacheTag(tag: Tag) {
        tagsSet.add(tag.value)
    }

    fun removeTag(id: String) {
        tagsSet.remove(id)
    }

    private val searchSet = TreeSet<String>()
    val searches: List<String> get() = searchSet.toList()

    fun cacheSearches(list: List<Search>) {
        list.forEach { cacheSearch(it) }
    }

    fun cacheSearch(search: Search) {
        searchSet.add(search.value)
    }

    fun removeSearch(id: String) {
        searchSet.remove(id)
    }
}
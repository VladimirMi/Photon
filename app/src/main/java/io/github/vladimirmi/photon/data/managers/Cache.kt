package io.github.vladimirmi.photon.data.managers

import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.dto.PhotocardDto
import io.github.vladimirmi.photon.data.models.dto.UserDto
import io.github.vladimirmi.photon.data.models.realm.*
import java.util.TreeSet
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap
import kotlin.collections.List
import kotlin.collections.forEach
import kotlin.collections.toList

/**
 * Created by Vladimir Mikhalev 17.07.2017.
 */

class Cache {
    private val photocardMap = LinkedHashMap<String, PhotocardDto>()
    val photocards: List<PhotocardDto> get() = photocardMap.values.toList()
    fun photocard(id: String) = photocardMap[id]

    fun cachePhotos(list: List<Photocard>): List<PhotocardDto> {
        return ArrayList<PhotocardDto>().apply {
            list.forEach { cachePhotocard(it)?.let { add(it) } }
        }
    }

    fun cachePhotocard(photocard: Photocard): PhotocardDto? {
        return if (photocard.active) {
            PhotocardDto(photocard).apply {
                photocardMap.put(photocard.id, this)
            }
        } else return null
    }

    fun removePhoto(id: String) {
        photocardMap.remove(id)
    }


    private val albumMap = LinkedHashMap<String, AlbumDto>()
    val albums: List<AlbumDto> get() = albumMap.values.toList()
    fun album(id: String) = albumMap[id]

    fun cacheAlbums(list: List<Album>): List<AlbumDto> {
        return ArrayList<AlbumDto>().apply {
            list.forEach { cacheAlbum(it)?.let { add(it) } }
        }
    }

    fun cacheAlbum(album: Album): AlbumDto? {
        return if (album.active) {
            AlbumDto(album).also {
                albumMap.put(album.id, it)
            }
        } else null
    }

    fun removeAlbum(id: String) {
        albumMap.remove(id)
    }


    private val userMap = LinkedHashMap<String, UserDto>()
    val users: List<UserDto> get() = userMap.values.toList()
    fun user(id: String) = userMap[id]

    fun cacheUsers(list: List<User>): ArrayList<UserDto> {
        return ArrayList<UserDto>().apply {
            list.forEach { cacheUser(it)?.let { add(it) } }
        }
    }

    fun cacheUser(user: User): UserDto? {
        return if (user.active) {
            UserDto(user).also {
                userMap.put(user.id, it)
            }
        } else null
    }

    fun removeUser(id: String) {
        userMap.remove(id)
    }


    private val tagsSet = TreeSet<String>()
    val tags: List<String> get() = tagsSet.toList()

    fun cacheTags(list: List<Tag>): List<String> {
        return ArrayList<String>().apply {
            list.forEach { add(cacheTag(it)) }
        }
    }

    fun cacheTag(tag: Tag): String {
        return tag.value.also { tagsSet.add(it) }
    }

    fun removeTag(id: String) {
        tagsSet.remove(id)
    }


    private val searchSet = TreeSet<String>()
    val searches: List<String> get() = searchSet.toList()

    fun cacheSearches(list: List<Search>): List<String> {
        return ArrayList<String>().apply {
            list.forEach { add(cacheSearch(it)) }
        }
    }

    fun cacheSearch(search: Search): String {
        return search.value.also { searchSet.add(it) }
    }

    fun removeSearch(id: String) {
        searchSet.remove(id)
    }
}
package io.github.vladimirmi.photon.data.managers

import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.dto.Cached
import io.github.vladimirmi.photon.data.models.dto.PhotocardDto
import io.github.vladimirmi.photon.data.models.dto.UserDto
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.models.realm.User
import io.realm.RealmObject
import java.lang.UnsupportedOperationException

/**
 * Created by Vladimir Mikhalev 17.07.2017.
 */

class Cache {
    //todo разделить по сущностям, переименовать в мапперы
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

    fun cache(obj: RealmObject): Cached? =
            when (obj) {
                is Album -> cacheAlbum(obj)
                is Photocard -> cachePhotocard(obj)
                is User -> cacheUser(obj)
                else -> throw UnsupportedOperationException()
        }
}
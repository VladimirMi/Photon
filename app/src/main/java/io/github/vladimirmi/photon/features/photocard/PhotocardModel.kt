package io.github.vladimirmi.photon.features.photocard

import io.github.vladimirmi.photon.data.managers.extensions.JobStatus
import io.github.vladimirmi.photon.data.mappers.PhotocardCachingMapper
import io.github.vladimirmi.photon.data.mappers.UserCachingMapper
import io.github.vladimirmi.photon.data.models.dto.PhotocardDto
import io.github.vladimirmi.photon.data.models.dto.UserDto
import io.github.vladimirmi.photon.data.repository.album.AlbumRepository
import io.github.vladimirmi.photon.data.repository.photocard.PhotocardRepository
import io.github.vladimirmi.photon.data.repository.profile.ProfileRepository
import io.github.vladimirmi.photon.data.repository.user.UserRepository
import io.github.vladimirmi.photon.utils.ioToMain
import io.github.vladimirmi.photon.utils.justOrEmpty
import io.reactivex.Observable

/**
 * Created by Vladimir Mikhalev 14.06.2017.
 */

class PhotocardModel(private val photocardRepository: PhotocardRepository,
                     private val userRepository: UserRepository,
                     private val albumRepository: AlbumRepository,
                     private val profileRepository: ProfileRepository,
                     private val userMapper: UserCachingMapper,
                     private val photocardMapper: PhotocardCachingMapper) : IPhotocardModel {

    override fun getUser(id: String): Observable<UserDto> {
        return Observable.merge(
                userRepository.updateUser(id).ignoreElement().toObservable(),
                userRepository.getUser(id).map { userMapper.map(it) },
                justOrEmpty(userMapper.get(id)))
                .ioToMain()
    }

    override fun getPhotocard(id: String): Observable<PhotocardDto> {
        return Observable.merge(
                photocardRepository.updatePhotocard(id).ignoreElement().toObservable(),
                photocardRepository.getPhotocard(id).map { photocardMapper.map(it) },
                justOrEmpty(photocardMapper.get(id)))
                .ioToMain()
    }

    override fun addToFavorite(id: String): Observable<JobStatus> =
            albumRepository.addFavorite(id).ioToMain()

    override fun removeFromFavorite(id: String): Observable<JobStatus> =
            albumRepository.deleteFavorite(id).ioToMain()


    override fun isFavorite(id: String): Observable<Boolean> {
        return profileRepository.getFavAlbum()
                .map { it.photocards.find { it.id == id } != null }
                .ioToMain()
    }
}
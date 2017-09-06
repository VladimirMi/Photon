package io.github.vladimirmi.photon.features.profile

import io.github.vladimirmi.photon.data.managers.extensions.JobStatus
import io.github.vladimirmi.photon.data.mappers.AlbumCachingMapper
import io.github.vladimirmi.photon.data.mappers.UserCachingMapper
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.dto.UserDto
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.req.ProfileEditReq
import io.github.vladimirmi.photon.data.repository.album.AlbumRepository
import io.github.vladimirmi.photon.data.repository.profile.ProfileRepository
import io.github.vladimirmi.photon.utils.ioToMain
import io.github.vladimirmi.photon.utils.justOrEmpty
import io.reactivex.Observable

class ProfileModel(private val profileRepository: ProfileRepository,
                   private val albumRepository: AlbumRepository,
                   private val userMapper: UserCachingMapper,
                   private val albumMapper: AlbumCachingMapper)
    : IProfileModel {

    override fun isUserAuth() = profileRepository.isUserAuth()

    override fun getProfile(): Observable<UserDto> =
            Observable.merge(
                    profileRepository.updateProfile().toObservable(),
                    profileRepository.getProfile().map { userMapper.map(it) },
                    justOrEmpty(userMapper.get(profileRepository.getProfileId())))
                    .ioToMain()

    override fun getAlbums(): Observable<List<AlbumDto>> =
            profileRepository.getAlbums()
                    .map { albumMapper.map(it) }
                    .ioToMain()

    override fun createAlbum(album: Album): Observable<JobStatus> =
            albumRepository.create(album).ioToMain()

    override fun editProfile(request: ProfileEditReq): Observable<JobStatus> =
            profileRepository.edit(request).ioToMain()

}
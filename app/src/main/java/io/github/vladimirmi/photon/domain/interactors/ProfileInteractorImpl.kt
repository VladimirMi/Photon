package io.github.vladimirmi.photon.domain.interactors

import io.github.vladimirmi.photon.data.managers.utils.JobStatus
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.req.ProfileEditReq
import io.github.vladimirmi.photon.data.repository.album.AlbumRepository
import io.github.vladimirmi.photon.data.repository.profile.ProfileRepository
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.domain.mappers.AlbumCachingMapper
import io.github.vladimirmi.photon.domain.mappers.UserCachingMapper
import io.github.vladimirmi.photon.domain.models.AlbumDto
import io.github.vladimirmi.photon.domain.models.UserDto
import io.github.vladimirmi.photon.presentation.profile.ProfileInteractor
import io.github.vladimirmi.photon.presentation.profile.ProfileScreen
import io.github.vladimirmi.photon.utils.ioToMain
import io.github.vladimirmi.photon.utils.justOrEmpty
import io.reactivex.Observable
import javax.inject.Inject

@DaggerScope(ProfileScreen::class)
class ProfileInteractorImpl
@Inject constructor(private val profileRepository: ProfileRepository,
                    private val albumRepository: AlbumRepository,
                    private val userMapper: UserCachingMapper,
                    private val albumMapper: AlbumCachingMapper)
    : ProfileInteractor {

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
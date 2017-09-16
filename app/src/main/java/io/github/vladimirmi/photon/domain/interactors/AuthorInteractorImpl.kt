package io.github.vladimirmi.photon.domain.interactors

import io.github.vladimirmi.photon.data.repository.user.UserRepository
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.domain.mappers.AlbumCachingMapper
import io.github.vladimirmi.photon.domain.mappers.UserCachingMapper
import io.github.vladimirmi.photon.domain.models.AlbumDto
import io.github.vladimirmi.photon.domain.models.UserDto
import io.github.vladimirmi.photon.presentation.author.AuthorInteractor
import io.github.vladimirmi.photon.presentation.author.AuthorScreen
import io.github.vladimirmi.photon.utils.ioToMain
import io.github.vladimirmi.photon.utils.justOrEmpty
import io.reactivex.Observable
import javax.inject.Inject

@DaggerScope(AuthorScreen::class)
class AuthorInteractorImpl
@Inject constructor(private val userRepository: UserRepository,
                    private val userMapper: UserCachingMapper,
                    private val albumsMapper: AlbumCachingMapper)
    : AuthorInteractor {

    override fun getUser(id: String): Observable<UserDto> {
        return Observable.merge(
                userRepository.updateUser(id).toObservable(),
                userRepository.getUser(id).map { userMapper.map(it) },
                justOrEmpty(userMapper.get(id)))
                .ioToMain()
    }

    override fun getAlbums(ownerId: String): Observable<List<AlbumDto>> =
            userRepository.getAlbums(ownerId)
                    .map { albumsMapper.map(it) }
                    .ioToMain()
}
package io.github.vladimirmi.photon.features.author

import io.github.vladimirmi.photon.data.mappers.AlbumCachingMapper
import io.github.vladimirmi.photon.data.mappers.UserCachingMapper
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.dto.UserDto
import io.github.vladimirmi.photon.data.repository.user.UserRepository
import io.github.vladimirmi.photon.utils.ioToMain
import io.github.vladimirmi.photon.utils.justOrEmpty
import io.reactivex.Observable

class AuthorModel(private val userRepository: UserRepository,
                  private val userMapper: UserCachingMapper,
                  private val albumsMapper: AlbumCachingMapper) : IAuthorModel {

    override fun getUser(id: String): Observable<UserDto> {
        return Observable.merge(
                userRepository.updateUser(id).ignoreElement().toObservable(),
                userRepository.getUser(id).map { userMapper.map(it) },
                justOrEmpty(userMapper.get(id)))
                .ioToMain()
    }

    override fun getAlbums(ownerId: String): Observable<List<AlbumDto>> =
            userRepository.getAlbums(ownerId)
                    .map { albumsMapper.map(it) }
                    .ioToMain()
}
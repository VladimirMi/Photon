package io.github.vladimirmi.photon.presentation.author

import io.github.vladimirmi.photon.core.Interactor
import io.github.vladimirmi.photon.domain.models.AlbumDto
import io.github.vladimirmi.photon.domain.models.UserDto
import io.reactivex.Observable

interface AuthorInteractor : Interactor {
    fun getUser(id: String): Observable<UserDto>
    fun getAlbums(ownerId: String): Observable<List<AlbumDto>>
}
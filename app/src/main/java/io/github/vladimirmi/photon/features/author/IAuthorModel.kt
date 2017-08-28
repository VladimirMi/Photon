package io.github.vladimirmi.photon.features.author

import io.github.vladimirmi.photon.core.IModel
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.dto.UserDto
import io.reactivex.Completable
import io.reactivex.Observable

interface IAuthorModel : IModel {
    fun getUser(id: String): Observable<UserDto>
    fun getAlbums(ownerId: String): Observable<List<AlbumDto>>
    fun updateUser(id: String): Completable
}
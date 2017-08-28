package io.github.vladimirmi.photon.features.profile

import io.github.vladimirmi.photon.core.IModel
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.dto.UserDto
import io.github.vladimirmi.photon.utils.JobStatus
import io.reactivex.Completable
import io.reactivex.Observable

interface IProfileModel : IModel {
    fun getProfile(): Observable<UserDto>
    fun getAlbums(): Observable<List<AlbumDto>>
    fun isUserAuth(): Boolean
    fun createAlbum(albumDto: AlbumDto): Observable<JobStatus>
    fun editProfile(userDto: UserDto): Observable<JobStatus>
    fun updateProfile(): Completable
}
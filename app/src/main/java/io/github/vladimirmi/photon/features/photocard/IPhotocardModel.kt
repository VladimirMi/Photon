package io.github.vladimirmi.photon.features.photocard

import io.github.vladimirmi.photon.core.IModel
import io.github.vladimirmi.photon.data.managers.extensions.JobStatus
import io.github.vladimirmi.photon.data.models.dto.PhotocardDto
import io.github.vladimirmi.photon.data.models.dto.UserDto
import io.reactivex.Completable
import io.reactivex.Observable

interface IPhotocardModel : IModel {
    fun getUser(id: String): Observable<UserDto>
    fun getPhotocard(id: String): Observable<PhotocardDto>
    fun addToFavorite(id: String): Observable<JobStatus>
    fun isFavorite(id: String): Observable<Boolean>
    fun removeFromFavorite(id: String): Observable<JobStatus>
    fun updateUser(id: String): Completable
    fun updatePhotocard(id: String, ownerId: String): Completable
}
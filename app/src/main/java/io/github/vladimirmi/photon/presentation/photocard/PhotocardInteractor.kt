package io.github.vladimirmi.photon.presentation.photocard

import io.github.vladimirmi.photon.core.Interactor
import io.github.vladimirmi.photon.data.managers.utils.JobStatus
import io.github.vladimirmi.photon.domain.models.PhotocardDto
import io.github.vladimirmi.photon.domain.models.UserDto
import io.reactivex.Observable

interface PhotocardInteractor : Interactor {
    fun getUser(id: String): Observable<UserDto>
    fun getPhotocard(id: String): Observable<PhotocardDto>
    fun addToFavorite(id: String): Observable<JobStatus>
    fun isFavorite(id: String): Observable<Boolean>
    fun removeFromFavorite(id: String): Observable<JobStatus>
}
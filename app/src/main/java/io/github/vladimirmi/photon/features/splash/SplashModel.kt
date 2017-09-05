package io.github.vladimirmi.photon.features.splash

import io.github.vladimirmi.photon.data.repository.photocard.PhotocardRepository
import io.github.vladimirmi.photon.data.repository.profile.ProfileRepository
import io.github.vladimirmi.photon.utils.ioToMain
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

class SplashModel(private val profileRepository: ProfileRepository,
                  private val photocardRepository: PhotocardRepository) : ISplashModel {

    override fun updateAll(limit: Int): Completable =
            Completable.mergeDelayError(listOf(updatePhotocards(limit), updateProfile()))
                    .ioToMain()

    override fun dbIsNotEmpty(): Single<Boolean> =
            photocardRepository.getPhotocards()
                    .map { it.isNotEmpty() }
                    .firstOrError()
                    .ioToMain()

    private fun updatePhotocards(limit: Int): Completable =
            photocardRepository.updatePhotocards(0, limit)

    private fun updateProfile(): Completable =
            profileRepository.updateProfile().ignoreElement()
}

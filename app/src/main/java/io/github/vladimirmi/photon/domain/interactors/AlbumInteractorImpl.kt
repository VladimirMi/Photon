package io.github.vladimirmi.photon.domain.interactors

import io.github.vladimirmi.photon.data.managers.extensions.JobStatus
import io.github.vladimirmi.photon.data.models.req.AlbumEditReq
import io.github.vladimirmi.photon.data.repository.album.AlbumRepository
import io.github.vladimirmi.photon.data.repository.photocard.PhotocardRepository
import io.github.vladimirmi.photon.data.repository.profile.ProfileRepository
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.domain.mappers.AlbumCachingMapper
import io.github.vladimirmi.photon.domain.models.AlbumDto
import io.github.vladimirmi.photon.domain.models.PhotocardDto
import io.github.vladimirmi.photon.presentation.album.AlbumInteractor
import io.github.vladimirmi.photon.presentation.album.AlbumScreen
import io.github.vladimirmi.photon.utils.ioToMain
import io.github.vladimirmi.photon.utils.justOrEmpty
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 19.06.2017.
 */

@DaggerScope(AlbumScreen::class)
class AlbumInteractorImpl
@Inject constructor(private val profileRepository: ProfileRepository,
                    private val photocardRepository: PhotocardRepository,
                    private val albumRepository: AlbumRepository,
                    private val albumMapper: AlbumCachingMapper) : AlbumInteractor {

    override fun getAlbum(id: String): Observable<AlbumDto> {
        return Observable.merge(
                albumRepository.updateAlbum(id).toObservable(),
                albumRepository.getAlbum(id).map { albumMapper.map(it) },
                justOrEmpty(albumMapper.get(id)))
                .ioToMain()
    }

    override fun isOwner(owner: String) = profileRepository.getProfileId() == owner

    override fun editAlbum(request: AlbumEditReq): Observable<JobStatus> =
            albumRepository.edit(request).ioToMain()

    override fun deleteAlbum(id: String): Observable<JobStatus> =
            albumRepository.delete(id).ioToMain()

    override fun removePhotos(photosForDelete: List<PhotocardDto>, album: AlbumDto): Observable<JobStatus> =
            removePhotosById(photosForDelete.map { it.id }, album.isFavorite).ioToMain()


    private fun removePhotosById(photosForDelete: List<String>,
                                 isFavorite: Boolean): Observable<JobStatus> {
        return if (photosForDelete.isEmpty()) {
            Observable.empty()
        } else {
            Observable.fromIterable(photosForDelete)
                    .flatMap {
                        if (isFavorite) {
                            albumRepository.deleteFavorite(it)
                        } else {
                            photocardRepository.delete(it)
                        }
                    }
        }
    }
}
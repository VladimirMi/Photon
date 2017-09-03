package io.github.vladimirmi.photon.data.jobs

import com.birbit.android.jobqueue.JobManager
import io.github.vladimirmi.photon.data.jobs.album.*
import io.github.vladimirmi.photon.data.jobs.photocard.PhotocardAddViewJob
import io.github.vladimirmi.photon.data.jobs.photocard.PhotocardCreateJob
import io.github.vladimirmi.photon.data.jobs.photocard.PhotocardDeleteJob
import io.github.vladimirmi.photon.data.jobs.profile.ProfileEditJob
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.managers.extensions.*
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.models.realm.extensions.*
import io.github.vladimirmi.photon.data.models.req.AlbumEditReq
import io.github.vladimirmi.photon.data.models.req.ProfileEditReq
import io.reactivex.Observable

/**
 * Created by Vladimir Mikhalev 16.08.2017.
 */


class Jobs(private val dm: DataManager,
           private val jobManager: JobManager) {

    //region =============== PROFILE ==============

    fun profileEdit(request: ProfileEditReq): Observable<JobStatus> =
            jobManager.jobStatusObservable(ProfileEditJob.TAG + dm.getProfileId()) {
                dm.getProfile().edit(request)
            }

    //endregion


    //region =============== ALBUM ==============

    fun albumCreate(album: Album): Observable<JobStatus> =
            jobManager.jobStatusObservable(AlbumCreateJob.TAG + album.id) {
                album.create()
            }

    fun albumDelete(id: String): Observable<JobStatus> =
            jobManager.jobStatusObservable(AlbumDeleteJob.TAG + id) {
                dm.getAlbum(id).delete()
            }

    fun albumEdit(request: AlbumEditReq): Observable<JobStatus> =
            jobManager.jobStatusObservable(AlbumEditJob.TAG + request.id) {
                dm.getAlbum(request.id).edit(request)
            }

    fun albumAddFavorite(id: String): Observable<JobStatus> =
            jobManager.jobStatusObservable(AlbumAddFavoritePhotoJob.TAG + id) {
                dm.getAlbum(dm.getUserFavAlbumId()).addFavorite(id)
            }

    fun albumDeleteFavorite(id: String): Observable<JobStatus> =
            jobManager.jobStatusObservable(AlbumDeleteFavoritePhotoJob.TAG + id) {
                dm.getAlbum(dm.getUserFavAlbumId()).deleteFavorite(id)
            }

    //endregion


    //region =============== PHOTOCARD ==============

    fun photocardCreate(photocard: Photocard): Observable<JobStatus> =
            jobManager.jobStatusObservable(PhotocardCreateJob.TAG + photocard.id) {
                photocard.create()
            }

    fun photocardDelete(id: String): Observable<JobStatus> =
            jobManager.jobStatusObservable(PhotocardDeleteJob.TAG + id) {
                dm.getPhotocard(id).delete()
            }

    fun photocardAddView(id: String): Observable<JobStatus> =
            jobManager.jobStatusObservable(PhotocardAddViewJob.TAG + id) {
                dm.getPhotocard(id).addView()
            }

    //endregion
}

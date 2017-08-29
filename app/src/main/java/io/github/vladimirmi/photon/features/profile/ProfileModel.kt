package io.github.vladimirmi.photon.features.profile

import io.github.vladimirmi.photon.data.jobs.Jobs
import io.github.vladimirmi.photon.data.managers.Cache
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.dto.UserDto
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.utils.JobStatus
import io.github.vladimirmi.photon.utils.Query
import io.github.vladimirmi.photon.utils.RealmOperator
import io.github.vladimirmi.photon.utils.ioToMain
import io.reactivex.Completable
import io.reactivex.Observable

class ProfileModel(private val dataManager: DataManager,
                   private val jobs: Jobs,
                   private val cache: Cache)
    : IProfileModel {

    override fun isUserAuth() = dataManager.isUserAuth()

    override fun getProfile(): Observable<UserDto> {
        return dataManager.getCached<User, UserDto>(dataManager.getProfileId())
                .ioToMain()
    }

    override fun getAlbums(): Observable<List<AlbumDto>> {
        val query = listOf(Query("owner", RealmOperator.EQUALTO, dataManager.getProfileId()))
        return dataManager.search(Album::class.java, query)
                .map { cache.cacheAlbums(it) }
                .ioToMain()
    }

    override fun updateProfile(): Completable {
        return dataManager.isNetworkAvailable()
                .filter { it }
                .flatMap { dataManager.getUserFromNet(dataManager.getProfileId()) }
                .doOnNext { dataManager.saveFromNet(it) }
                .ignoreElements()
                .ioToMain()
    }

    override fun createAlbum(albumDto: AlbumDto): Observable<JobStatus> =
            jobs.albumCreate(albumDto).ioToMain()

    override fun editProfile(userDto: UserDto): Observable<JobStatus> =
            jobs.profileEdit(userDto).ioToMain()

}
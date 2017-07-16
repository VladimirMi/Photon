package io.github.vladimirmi.photon.features.profile

import com.birbit.android.jobqueue.JobManager
import io.github.vladimirmi.photon.data.jobs.EditProfileJob
import io.github.vladimirmi.photon.data.jobs.singleResultFor
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.EditProfileReq
import io.github.vladimirmi.photon.data.models.NewAlbumReq
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.dto.UserDto
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.utils.ErrorObserver
import io.github.vladimirmi.photon.utils.Query
import io.github.vladimirmi.photon.utils.RealmOperator
import io.github.vladimirmi.photon.utils.ioToMain
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class ProfileModel(val dataManager: DataManager, val jobManager: JobManager) : IProfileModel {

    override fun isUserAuth(): Boolean {
        return dataManager.isUserAuth()
    }

    override fun getProfile(): Observable<UserDto> {
        val id = dataManager.getProfileId()
        updateUser(id)
        return dataManager.getObjectFromDb(User::class.java, id)
                .map { UserDto(it) }
    }

    override fun getAlbums(): Observable<List<AlbumDto>> {
        val query = listOf(Query("owner", RealmOperator.EQUALTO, dataManager.getProfileId()))
        return dataManager.search(Album::class.java, query, sortBy = "id", async = false)
                .map { it.filter { it.active }.map { AlbumDto(it) } }
    }

    private fun updateUser(id: String) {
        val user = dataManager.getDetachedObjFromDb(User::class.java, id)

        dataManager.getUserFromNet(id, getUpdated(user).toString())
                .subscribeOn(Schedulers.io())
                .subscribeWith(object : ErrorObserver<User>() {
                    override fun onNext(it: User) {
                        dataManager.saveToDB(it)
                    }
                })
    }

    override fun createAlbum(newAlbumReq: NewAlbumReq): Observable<Unit> {
        newAlbumReq.owner = dataManager.getProfileId()
        return dataManager.createAlbum(newAlbumReq)
                .map {
                    val profile = dataManager.getDetachedObjFromDb(User::class.java, dataManager.getProfileId())!!
                    profile.albums.add(it)
                    dataManager.saveToDB(profile)
                }
                .ioToMain()
    }

    override fun editProfile(profileReq: EditProfileReq, loadAvatar: Boolean): Single<Unit> {
        val profile = dataManager.getDetachedObjFromDb(User::class.java, dataManager.getProfileId())!!
        profileReq.id = profile.id
        if (profileReq.avatar.isEmpty()) profileReq.avatar = profile.avatar
        val job = EditProfileJob(profileReq, avatarLoad = profile.avatar != profileReq.avatar)

        return Single.just(profile)
                .map { changeProfile(it, profileReq) }
                .map { dataManager.saveToDB(it) }
                .map { jobManager.addJobInBackground(job) }
                .flatMap { jobManager.singleResultFor(job) }
                .doOnError { rollBack(profile) }
                .ioToMain()
    }

    private lateinit var backup: EditProfileReq

    private fun changeProfile(profile: User, profileReq: EditProfileReq): User {
        backup = EditProfileReq(name = profile.name,
                login = profile.login,
                avatar = profile.avatar)
        return profile.apply {
            login = profileReq.login
            name = profileReq.name
            avatar = profileReq.avatar
        }
    }

    private fun rollBack(profile: User) {
        dataManager.saveToDB(changeProfile(profile, backup))
    }
}
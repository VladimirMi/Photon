package io.github.vladimirmi.photon.features.profile

import com.birbit.android.jobqueue.JobManager
import io.github.vladimirmi.photon.data.jobs.UploadAvatarJob
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.EditProfileReq
import io.github.vladimirmi.photon.data.models.NewAlbumReq
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.utils.ErrorObserver
import io.github.vladimirmi.photon.utils.ioToMain
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers

class ProfileModel(val dataManager: DataManager, val jobManager: JobManager) : IProfileModel {

    override fun isUserAuth(): Boolean {
        return dataManager.isUserAuth()
    }

    override fun getProfile(): Observable<User> {
        val id = dataManager.getProfileId()
        return getUser(id)
    }

    override fun getUser(userId: String): Observable<User> {
        updateUser(userId)
        return dataManager.getObjectFromDb(User::class.java, userId)
    }

    private fun updateUser(id: String) {
        val user = dataManager.getSingleObjFromDb(User::class.java, id)

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
                .zipWith(getProfile(), BiFunction { album: Album, profile: User ->
                    profile.albums.add(album)
                    dataManager.saveToDB(profile)
                })
                .ioToMain()
    }

    override fun editProfile(profile: User): Observable<User> {
        return dataManager.editProfile(
                EditProfileReq(id = profile.id,
                        name = profile.name,
                        login = profile.login,
                        avatar = profile.avatar))
                .doOnNext { dataManager.saveToDB(it) }
                .ioToMain()
    }

    override fun saveAvatar(uri: String, profile: User) {
        if (profile.avatar != uri) {
            profile.avatar = uri
            jobManager.addJobInBackground(UploadAvatarJob(profile))
        }
    }
}
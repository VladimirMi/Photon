package io.github.vladimirmi.photon.features.profile

import com.birbit.android.jobqueue.JobManager
import io.github.vladimirmi.photon.data.jobs.EditProfileJob
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.EditProfileReq
import io.github.vladimirmi.photon.data.models.NewAlbumReq
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.data.network.ApiError
import io.github.vladimirmi.photon.utils.ErrorObserver
import io.github.vladimirmi.photon.utils.ioToMain
import io.reactivex.Observable
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

    override fun createAlbum(newAlbumReq: NewAlbumReq, profile: User): Observable<Unit> {
        newAlbumReq.owner = profile.id
        return dataManager.createAlbum(newAlbumReq)
                .map {
                    profile.albums.add(it)
                    dataManager.saveToDB(profile)
                }
                .ioToMain()
    }

    override fun editProfile(profileReq: EditProfileReq, avatarChange: Boolean, errCallback: (ApiError?) -> Unit) {
        jobManager.addJobInBackground(EditProfileJob(profileReq, avatarChange, errCallback))
    }
}
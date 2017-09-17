package io.github.vladimirmi.photon.data.repository.profile

import com.birbit.android.jobqueue.JobManager
import io.github.vladimirmi.photon.core.App
import io.github.vladimirmi.photon.data.jobs.profile.ProfileEditJob
import io.github.vladimirmi.photon.data.managers.PreferencesManager
import io.github.vladimirmi.photon.data.managers.RealmManager
import io.github.vladimirmi.photon.data.managers.utils.JobStatus
import io.github.vladimirmi.photon.data.managers.utils.addAndObserve
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.data.models.req.ProfileEditReq
import io.github.vladimirmi.photon.data.models.req.SignInReq
import io.github.vladimirmi.photon.data.models.req.SignUpReq
import io.github.vladimirmi.photon.data.network.NetworkChecker
import io.github.vladimirmi.photon.data.network.api.RestService
import io.github.vladimirmi.photon.data.network.body
import io.github.vladimirmi.photon.data.network.parseStatusCode
import io.github.vladimirmi.photon.data.repository.user.UserRepository
import io.github.vladimirmi.photon.di.DaggerScope
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 04.09.2017.
 */

@DaggerScope(App::class)
class ProfileRepository
@Inject constructor(realmManager: RealmManager,
                    private val restService: RestService,
                    private val preferencesManager: PreferencesManager,
                    private val networkChecker: NetworkChecker,
                    private val jobManager: JobManager,
                    private val userRepository: UserRepository)
    : ProfileEntityRepository(realmManager) {

    fun signIn(req: SignInReq): Completable {
        return restService.signIn(req)
                .parseStatusCode()
                .body()
                .doOnSuccess { saveUser(it) }
                .toCompletable()
    }

    fun signUp(req: SignUpReq): Completable {
        return restService.signUp(req)
                .parseStatusCode()
                .body()
                .doOnSuccess { saveUser(it) }
                .toCompletable()
    }

    fun logout() {
        realmManager.remove(User::class.java, getProfileId())
        preferencesManager.clearUser()
    }

    fun getProfile(managed: Boolean = true): Observable<User> =
            userRepository.getUser(getProfileId(), managed).share()

    fun updateProfile(): Completable {
        if (!isUserAuth()) return Completable.complete()
        return userRepository.updateUser(getProfileId())
    }

    fun getAlbums() = userRepository.getAlbums(getProfileId())

    fun getFavAlbum(managed: Boolean = true): Observable<Album> =
            getProfile(managed).map { it.albums.find { it.isFavorite } ?: throw NoSuchElementException() }

    fun getProfileId() = preferencesManager.getProfileId()

    fun getFavAlbumId() = preferencesManager.getFavAlbumId()

    fun isUserAuth() = preferencesManager.isUserAuth()

    fun isNetAvail() = networkChecker.isAvailable()

    fun edit(request: ProfileEditReq): Observable<JobStatus> =
            Observable.fromCallable { getUser(getProfileId()).edit(request) }
                    .flatMap { jobManager.addAndObserve(ProfileEditJob(getProfileId())) }

    private fun saveUser(user: User) {
        saveFromNet(user)
        preferencesManager.saveUserId(user.id)
        preferencesManager.saveUserToken(user.token)
        preferencesManager.saveFavAlbumId(user.albums.find { it.isFavorite }!!.id)
    }
}
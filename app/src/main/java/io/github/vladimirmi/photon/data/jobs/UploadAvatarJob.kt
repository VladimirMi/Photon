package io.github.vladimirmi.photon.data.jobs

import android.net.Uri
import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import io.github.vladimirmi.photon.data.models.EditProfileReq
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.AppConfig
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import timber.log.Timber

/**
 * Created by Vladimir Mikhalev 25.06.2017.
 */

class UploadAvatarJob(private val profile: User)
    : Job(Params(JobPriority.HIGH)
        .requireNetwork()) {


    override fun onAdded() {
        Timber.e("onAdded: ")
    }

    override fun onRun() {
        Timber.e("onRun: ")
        val dataManager = DaggerService.appComponent.dataManager()
        val data = getByteArrayFromContent(profile.avatar)
        val body = RequestBody.create(MediaType.parse("multipart/form-data"), data)

        val bodyPart = MultipartBody.Part.createFormData("image", Uri.parse(profile.avatar).lastPathSegment, body)
        dataManager.uploadPhoto(bodyPart)
                .flatMap {
                    profile.avatar = it.image
                    dataManager.editProfile(
                            EditProfileReq(id = profile.id,
                                    name = profile.name,
                                    login = profile.login,
                                    avatar = profile.avatar))
                }
                .subscribeOn(Schedulers.io())
                .subscribe { dataManager.saveToDB(it) }

    }

    private fun getByteArrayFromContent(contentUri: String): ByteArray {
        val inputStream = DaggerService.appComponent.context().contentResolver
                .openInputStream(Uri.parse(contentUri))
        val result = inputStream.readBytes()
        inputStream.close()
        return result
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        Timber.e("onCancel: ")
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int): RetryConstraint {
        return RetryConstraint.createExponentialBackoff(runCount, AppConfig.INITIAL_BACK_OFF_IN_MS)
    }

}

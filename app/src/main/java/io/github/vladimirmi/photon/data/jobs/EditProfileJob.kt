package io.github.vladimirmi.photon.data.jobs

import android.net.Uri
import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import io.github.vladimirmi.photon.data.models.EditProfileReq
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.data.network.ApiError
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.AppConfig
import io.github.vladimirmi.photon.utils.ErrorObserver
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import timber.log.Timber

/**
 * Created by Vladimir Mikhalev 25.06.2017.
 */

class EditProfileJob(private val profileReq: EditProfileReq,
                     private val avatarChange: Boolean = false,
                     private val errCallback: (ApiError?) -> Unit)
    : Job(Params(JobPriority.HIGH)
        .requireNetwork()) {


    override fun onAdded() {
        Timber.e("onAdded: ")
    }

    override fun onRun() {
        Timber.e("onRun: ")
        var throwError = false
        val dataManager = DaggerService.appComponent.dataManager()

        val editProfileObs = dataManager.editProfile(profileReq)

        val obs = if (avatarChange) {
            val data = getByteArrayFromContent(profileReq.avatar)
            val body = RequestBody.create(MediaType.parse("multipart/form-data"), data)
            val bodyPart = MultipartBody.Part.createFormData("image", Uri.parse(profileReq.avatar).lastPathSegment, body)

            dataManager.uploadPhoto(bodyPart)
                    .doOnNext { profileReq.avatar = it.image }
                    .flatMap { editProfileObs }
        } else {
            editProfileObs
        }

        obs.subscribeWith(object : ErrorObserver<User>() {
            override fun onNext(it: User) {
                dataManager.saveToDB(it)
                errCallback(null)
            }

            override fun onError(e: Throwable) {
                super.onError(e)
                throwError = true
                if (e is ApiError && e.errorResId != e.defaultErr) {
                    throwError = false
                    errCallback(e)
                }
            }
        })
        if (throwError) throw Throwable()
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

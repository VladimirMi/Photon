package io.github.vladimirmi.photon.data.jobs

import android.net.Uri
import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import io.github.vladimirmi.photon.data.models.EditProfileReq
import io.github.vladimirmi.photon.data.network.ApiError
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.AppConfig
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody

/**
 * Created by Vladimir Mikhalev 25.06.2017.
 */

class EditProfileJob(private val profileReq: EditProfileReq,
                     private val avatarLoad: Boolean = false)
    : Job(Params(JobPriority.HIGH)
        .requireNetwork()
        .persist()) {


    override fun onAdded() {}

    override fun onRun() {
        val dataManager = DaggerService.appComponent.dataManager()

        val editProfileObs = dataManager.editProfile(profileReq)

        val observable = if (avatarLoad) {
            val data = getByteArrayFromContent(profileReq.avatar)
            val body = RequestBody.create(MediaType.parse("multipart/form-data"), data)
            val bodyPart = MultipartBody.Part.createFormData("image", Uri.parse(profileReq.avatar).lastPathSegment, body)

            dataManager.uploadPhoto(bodyPart)
                    .doOnNext { profileReq.avatar = it.image }
                    .flatMap { editProfileObs }
        } else {
            editProfileObs
        }

        var error: Throwable? = null
        observable.blockingSubscribe({ dataManager.saveToDB(it) }, { error = it })
        error?.let { throw it }
    }

    private fun getByteArrayFromContent(contentUri: String): ByteArray {
        val inputStream = DaggerService.appComponent.context().contentResolver
                .openInputStream(Uri.parse(contentUri))
        val result = inputStream.readBytes()
        inputStream.close()
        return result
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        logCancel(cancelReason, throwable)
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int): RetryConstraint {
        if (throwable is ApiError && throwable.errorResId != throwable.defaultErr) {
            return RetryConstraint.CANCEL
        }
        return RetryConstraint.createExponentialBackoff(runCount, AppConfig.INITIAL_BACK_OFF_IN_MS)
    }

}

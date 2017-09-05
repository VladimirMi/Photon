package io.github.vladimirmi.photon.data.jobs.profile

import android.net.Uri
import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import io.github.vladimirmi.photon.data.managers.extensions.JobPriority
import io.github.vladimirmi.photon.data.managers.extensions.cancelOrWaitConnection
import io.github.vladimirmi.photon.data.managers.extensions.logCancel
import io.github.vladimirmi.photon.data.models.req.ProfileEditReq
import io.github.vladimirmi.photon.data.repository.profile.ProfileJobRepository
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.ErrorSingleObserver
import io.reactivex.schedulers.Schedulers.io
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody

/**
 * Created by Vladimir Mikhalev 25.06.2017.
 */

class ProfileEditJob(private val repository: ProfileJobRepository)
    : Job(Params(JobPriority.HIGH)
        .addTags(TAG)
        .requireNetwork()) {

    companion object {
        const val TAG = "ProfileEditJob"
    }

    override fun onAdded() {}

    override fun onRun() {
        val profile = repository.getProfile()

        if (!profile.avatar.startsWith("http")) {
            val data = getByteArrayFromContent(profile.avatar)
            val body = RequestBody.create(MediaType.parse("multipart/form-data"), data)
            val bodyPart = MultipartBody.Part
                    .createFormData("image", Uri.parse(profile.avatar).lastPathSegment, body)
            val imageRes = repository.uploadPhoto(bodyPart).blockingGet()
            profile.avatar = imageRes.image
            repository.save(profile)
        }

        repository.editProfile(ProfileEditReq.from(profile)).blockingGet()
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        logCancel(cancelReason, throwable)
        rollback()
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int) =
            cancelOrWaitConnection(throwable, runCount)


    private fun getByteArrayFromContent(contentUri: String): ByteArray {
        DaggerService.appComponent.context().contentResolver.openInputStream(Uri.parse(contentUri))
                .use { return it.readBytes() }
    }

    private fun rollback() {
        repository.getProfileFromNet()
                .doOnSuccess { repository.rollbackEdit(ProfileEditReq.from(it)) }
                .subscribeOn(io())
                .subscribeWith(ErrorSingleObserver())
    }
}

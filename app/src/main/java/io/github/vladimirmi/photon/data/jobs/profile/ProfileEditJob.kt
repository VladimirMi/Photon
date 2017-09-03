package io.github.vladimirmi.photon.data.jobs.profile

import android.net.Uri
import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import io.github.vladimirmi.photon.data.managers.extensions.JobPriority
import io.github.vladimirmi.photon.data.managers.extensions.cancelOrWaitConnection
import io.github.vladimirmi.photon.data.managers.extensions.getProfile
import io.github.vladimirmi.photon.data.managers.extensions.logCancel
import io.github.vladimirmi.photon.data.models.realm.extensions.edit
import io.github.vladimirmi.photon.data.models.req.ProfileEditReq
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.ErrorObserver
import io.reactivex.schedulers.Schedulers.io
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody

/**
 * Created by Vladimir Mikhalev 25.06.2017.
 */

class ProfileEditJob(private val userId: String)
    : Job(Params(JobPriority.HIGH)
        .addTags(TAG + userId)
        .requireNetwork()) {

    companion object {
        const val TAG = "ProfileEditJob"
    }

    private val dataManager = DaggerService.appComponent.dataManager()
    private val profile = dataManager.getProfile()

    override fun onAdded() {}

    override fun onRun() {
        if (!profile.avatar.startsWith("http")) {
            val data = getByteArrayFromContent(profile.avatar)
            val body = RequestBody.create(MediaType.parse("multipart/form-data"), data)
            val bodyPart = MultipartBody.Part
                    .createFormData("image", Uri.parse(profile.avatar).lastPathSegment, body)
            val imageRes = dataManager.uploadPhoto(bodyPart).blockingGet()
            profile.avatar = imageRes.image
            dataManager.save(profile)
        }

        dataManager.editProfile(ProfileEditReq.from(profile)).blockingGet()
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        logCancel(cancelReason, throwable)
        rollbackProfile()
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int) =
            cancelOrWaitConnection(throwable, runCount)


    private fun getByteArrayFromContent(contentUri: String): ByteArray {
        DaggerService.appComponent.context().contentResolver.openInputStream(Uri.parse(contentUri))
                .use { return it.readBytes() }
    }

    private fun rollbackProfile() {
        dataManager.getUserFromNet(userId, force = true)
                .doOnNext { profile.edit(ProfileEditReq.from(it)) }
                .subscribeOn(io())
                .subscribeWith(ErrorObserver())
    }
}

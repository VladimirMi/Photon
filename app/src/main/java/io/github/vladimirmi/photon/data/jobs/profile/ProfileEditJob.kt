package io.github.vladimirmi.photon.data.jobs.profile

import android.net.Uri
import io.github.vladimirmi.photon.data.jobs.ChainJob
import io.github.vladimirmi.photon.data.managers.extensions.JobGroup
import io.github.vladimirmi.photon.data.models.req.ProfileEditReq
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.ErrorSingleObserver
import io.reactivex.schedulers.Schedulers.io
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody

/**
 * Created by Vladimir Mikhalev 25.06.2017.
 */

class ProfileEditJob(profileId: String)
    : ChainJob(TAG, JobGroup.PROFILE, profileId) {

    companion object {
        const val TAG = "ProfileEditJob"
    }

    override fun execute() {
        val repository = DaggerService.appComponent.profileJobRepository()
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
        super.onCancel(cancelReason, throwable)
        rollback()
    }

    private fun getByteArrayFromContent(contentUri: String): ByteArray {
        DaggerService.appComponent.context().contentResolver.openInputStream(Uri.parse(contentUri))
                .use { return it.readBytes() }
    }

    private fun rollback() {
        val repository = DaggerService.appComponent.profileJobRepository()
        repository.getProfileFromNet()
                .doOnSuccess { repository.rollbackEdit(ProfileEditReq.from(it)) }
                .subscribeOn(io())
                .subscribeWith(ErrorSingleObserver())
    }

    override fun copy() = throw UnsupportedOperationException()
}

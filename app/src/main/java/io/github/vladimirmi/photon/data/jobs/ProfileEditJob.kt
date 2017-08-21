package io.github.vladimirmi.photon.data.jobs

import android.net.Uri
import com.birbit.android.jobqueue.CancelReason
import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import io.github.vladimirmi.photon.data.jobs.queue.JobTask
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.data.models.req.EditProfileReq
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.*
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.util.*

/**
 * Created by Vladimir Mikhalev 25.06.2017.
 */

class ProfileEditJob(override val request: EditProfileReq)
    : Job(Params(JobPriority.HIGH)
        .setGroupId(JobGroup.PROFILE)
        .requireNetwork()
        .persist()), JobTask {

    companion object {
        const val TAG = "ProfileEditJob"
    }

    override var entityId = request.id
    override var parentEntityId
        get() = entityId
        set(value) {
            entityId = value
        }
    override val tag = TAG
    override val type = JobTask.Type.UNIQUE

    override fun onQueued() {
        val dataManager = DaggerService.appComponent.dataManager()
        val profile = dataManager.getDetachedObjFromDb(User::class.java, dataManager.getProfileId())!!

        profile.apply {
            login = request.login
            name = request.name
            avatar = request.avatar
        }
        dataManager.saveToDB(profile)
    }

    override fun onAdded() {}

    override fun onRun() {
        val dataManager = DaggerService.appComponent.dataManager()
        request.id = entityId

        val editProfileObs = dataManager.editProfile(request)

        val observable = if (request.avatarChanged) {
            val data = getByteArrayFromContent(request.avatar)
            val body = RequestBody.create(MediaType.parse("multipart/form-data"), data)
            val bodyPart = MultipartBody.Part.createFormData("image", Uri.parse(request.avatar).lastPathSegment, body)

            dataManager.uploadPhoto(bodyPart)
                    .doOnNext { request.avatar = it.image }
                    .flatMap { editProfileObs }
        } else {
            editProfileObs
        }

        var error: Throwable? = null
        observable.doOnNext { dataManager.saveToDB(it) }
                .blockingSubscribe({}, { error = it })

        error?.let { throw it }
    }

    private fun getByteArrayFromContent(contentUri: String): ByteArray {
        DaggerService.appComponent.context().contentResolver
                .openInputStream(Uri.parse(contentUri)).use {
            return it.readBytes()
        }
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        logCancel(cancelReason, throwable)
        if (cancelReason == CancelReason.CANCELLED_VIA_SHOULD_RE_RUN) {
            updateProfile()
        }
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int) =
            cancelOrWait(throwable, runCount)

    private fun updateProfile() {
        val dataManager = DaggerService.appComponent.dataManager()

        dataManager.getUserFromNet(dataManager.getProfileId(), Date(0).toString())
                .doOnNext { dataManager.saveToDB(it) }
                .subscribeOn(Schedulers.io())
                .subscribeWith(ErrorObserver())
    }

}

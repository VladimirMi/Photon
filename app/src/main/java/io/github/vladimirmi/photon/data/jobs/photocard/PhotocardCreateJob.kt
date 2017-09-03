package io.github.vladimirmi.photon.data.jobs.photocard

import android.net.Uri
import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import io.github.vladimirmi.photon.data.managers.extensions.JobPriority
import io.github.vladimirmi.photon.data.managers.extensions.cancelOrWaitConnection
import io.github.vladimirmi.photon.data.managers.extensions.getPhotocard
import io.github.vladimirmi.photon.data.managers.extensions.logCancel
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.di.DaggerService
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody

/**
 * Created by Vladimir Mikhalev 25.06.2017.
 */

class PhotocardCreateJob(private val photocardId: String)
    : Job(Params(JobPriority.HIGH)
        .addTags(TAG + photocardId)
        .requireNetwork()) {

    companion object {
        val TAG = "PhotocardCreateJob"
    }

    private val dataManager = DaggerService.appComponent.dataManager()
    private val cache = DaggerService.appComponent.cache()

    override fun onAdded() {}

    override fun onRun() {
        val photocard = dataManager.getPhotocard(photocardId)

        val data = getByteArrayFromContent(photocard.photo)
        val body = RequestBody.create(MediaType.parse("multipart/form-data"), data)
        val bodyPart = MultipartBody.Part.createFormData("image", Uri.parse(photocard.photo).lastPathSegment, body)

        val photocardRes = dataManager.uploadPhoto(bodyPart)
                .flatMap { imageUrlRes ->
                    photocard.photo = imageUrlRes.image
                    dataManager.createPhotocard(photocard)
                }.blockingGet()


        dataManager.removeFromDb(Photocard::class.java, photocardId)
        cache.removePhoto(photocardId)
        photocard.id = photocardRes.id
        dataManager.save(photocard)
    }

    private fun getByteArrayFromContent(contentUri: String): ByteArray {
        DaggerService.appComponent.context().contentResolver
                .openInputStream(Uri.parse(contentUri)).use {
            return it.readBytes()
        }
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        logCancel(cancelReason, throwable)
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int) =
            cancelOrWaitConnection(throwable, runCount)
}

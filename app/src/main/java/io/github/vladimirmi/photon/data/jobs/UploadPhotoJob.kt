package io.github.vladimirmi.photon.data.jobs

import android.net.Uri
import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import io.github.vladimirmi.photon.data.models.Photocard
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

class UploadPhotoJob(private val photocard: Photocard)
    : Job(Params(JobPriority.HIGH)
        .groupBy("Images")
        .requireNetwork()) {

    val tempId = photocard.id

    override fun onAdded() {
        Timber.e("onAdded: ")
        photocard.id = ""
        DaggerService.appComponent.dataManager().saveToDB(photocard)
    }

    override fun onRun() {
        Timber.e("onRun: ")
        val dataManager = DaggerService.appComponent.dataManager()
        val data = getByteArrayFromContent(photocard.photo)
        val body = RequestBody.create(MediaType.parse("multipart/form-data"), data)

        val bodyPart = MultipartBody.Part.createFormData("image", Uri.parse(photocard.photo).lastPathSegment, body)
        dataManager.uploadPhoto(bodyPart)
                .flatMap {
                    photocard.photo = it.image
                    dataManager.createPhotocard(photocard)
                }
                .map { photocard.id = it.id }
                .subscribeOn(Schedulers.io())
                .subscribe {
                    dataManager.removeFromDb(Photocard::class.java, tempId)
                    dataManager.saveToDB(photocard)
                }

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

package io.github.vladimirmi.photon.data.jobs

import android.net.Uri
import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.AppConfig
import io.github.vladimirmi.photon.utils.Constants
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody

/**
 * Created by Vladimir Mikhalev 25.06.2017.
 */

class CreatePhotoJob(private val photocardId: String)
    : Job(Params(JobPriority.HIGH)
        .groupBy("Images")
        .setSingleId(photocardId)
        .addTags(Constants.CREATE_PHOTOCART_JOB_TAG + photocardId)
        .requireNetwork()
        .persist()) {

    override fun onAdded() {}

    @Throws(Throwable::class)
    override fun onRun() {
        var error: Throwable? = null
        val dataManager = DaggerService.appComponent.dataManager()
        val photocard = dataManager.getDetachedObjFromDb(Photocard::class.java, photocardId)!!

        val data = getByteArrayFromContent(photocard.photo)
        val body = RequestBody.create(MediaType.parse("multipart/form-data"), data)
        val bodyPart = MultipartBody.Part.createFormData("image", Uri.parse(photocard.photo).lastPathSegment, body)

        dataManager.uploadPhoto(bodyPart)
                .flatMap {
                    photocard.photo = it.image
                    dataManager.createPhotocard(photocard)
                }
                .doOnNext {
                    photocard.id = it.id
                    dataManager.removeFromDb(Photocard::class.java, photocardId)
                    val album = dataManager.getDetachedObjFromDb(Album::class.java, photocard.album)!!
                    album.photocards.add(photocard)
                    dataManager.saveToDB(album)
                }
                .blockingSubscribe({}, { error = it })

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
        return RetryConstraint.createExponentialBackoff(runCount, AppConfig.INITIAL_BACK_OFF_IN_MS)
    }
}

package io.github.vladimirmi.photon.data.jobs

import android.net.Uri
import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody

/**
 * Created by Vladimir Mikhalev 25.06.2017.
 */

class PhotocardCreateJob(val photocardId: String,
                         val albumId: String)
    : Job(Params(JobPriority.HIGH)
        .groupBy(JobGroup.PHOTOCARD)
        .setSingleId(photocardId)
        .addTags(TAG + photocardId, JobGroup.PHOTOCARD + photocardId)
        .requireNetwork()
        .persist()), WithPayload {

    companion object {
        val TAG = "PhotocardCreateJob"
    }

    override val payload = Payload<String>()

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
                .flatMap { imageUrlRes ->
                    photocard.photo = imageUrlRes.image
                    photocard.album = albumId
                    dataManager.createPhotocard(photocard)
                }
                .doOnNext {
                    payload.value = it.id
                    photocard.id = it.id
                    val album = dataManager.getDetachedObjFromDb(Album::class.java, albumId)!!
                    album.photocards.add(photocard)
                    dataManager.saveToDB(album)
                    removeTempPhotocard()
                }
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
        if (throwable != null) {
            removeTempPhotocard()
        }
    }

    private fun removeTempPhotocard() {
        DaggerService.appComponent.dataManager().removeFromDb(Photocard::class.java, photocardId)
        DaggerService.appComponent.cache().removePhoto(photocardId)
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int): RetryConstraint {
        return RetryConstraint.createExponentialBackoff(runCount, AppConfig.INITIAL_BACK_OFF_IN_MS)
    }
}

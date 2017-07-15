package io.github.vladimirmi.photon.data.jobs

import android.net.Uri
import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.AppConfig
import io.github.vladimirmi.photon.utils.ErrorObserver
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody

/**
 * Created by Vladimir Mikhalev 25.06.2017.
 */

class UploadPhotoJob(private val photocard: Photocard)
    : Job(Params(JobPriority.HIGH)
        .groupBy("Images")
        .setSingleId(photocard.id)
        .requireNetwork()) {

    private val tempId = photocard.id

    override fun onAdded() {
        DaggerService.appComponent.dataManager().saveToDB(photocard)
    }

    @Throws(Throwable::class)
    override fun onRun() {
        var error: Throwable? = null
        val dataManager = DaggerService.appComponent.dataManager()

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
                    dataManager.removeFromDb(Photocard::class.java, tempId)
                    val album = dataManager.getDetachedObjFromDb(Album::class.java, photocard.album)!!
                    album.photocards.add(photocard)
                    dataManager.saveToDB(album)
                }
                .subscribeWith(object : ErrorObserver<Photocard>() {
                    override fun onError(e: Throwable) {
                        super.onError(e)
                        error = e
                    }
                })
        if (error != null) throw error!!
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

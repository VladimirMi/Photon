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
import timber.log.Timber

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
        Timber.e("onAdded: ")
        DaggerService.appComponent.dataManager().saveToDB(photocard)
    }

    @Throws(Throwable::class)
    override fun onRun() {
        Timber.e("onRun: ")
        var throwError = false
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
                }
                .flatMap { dataManager.getObjectFromDb(Album::class.java, photocard.album) }
                .subscribeWith(object : ErrorObserver<Album>() {
                    override fun onNext(it: Album) {
                        it.photocards.add(photocard)
                        dataManager.saveToDB(it)
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        throwError = true
                    }
                })
        if (throwError) throw Throwable()
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

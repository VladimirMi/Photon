package io.github.vladimirmi.photon.data.jobs

import android.net.Uri
import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import io.github.vladimirmi.photon.data.models.Photocard
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.AppConfig
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import timber.log.Timber

/**
 * Created by Vladimir Mikhalev 25.06.2017.
 */


class UploadPhotoJob(private val photocard: Photocard)
    : Job(Params(JobPriority.HIGH)
        .singleInstanceBy(photocard.id)
        .requireNetwork()
        .persist()) {

    override fun onAdded() {
        Timber.e("onAdded: ")
        //todo записать в бд то, что уже сейчас есть, как загрузится - заменить
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
                .map { photocard.id = it.id }  //todo проверить что приходит
                .subscribe { dataManager.saveToDB(photocard) }

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

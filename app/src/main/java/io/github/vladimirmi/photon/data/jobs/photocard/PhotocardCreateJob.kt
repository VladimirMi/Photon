package io.github.vladimirmi.photon.data.jobs.photocard

import android.net.Uri
import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.JobGroup
import io.github.vladimirmi.photon.utils.JobPriority
import io.github.vladimirmi.photon.utils.cancelOrWaitConnection
import io.github.vladimirmi.photon.utils.logCancel
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody

/**
 * Created by Vladimir Mikhalev 25.06.2017.
 */

class PhotocardCreateJob(private val photocardId: String)
    : Job(Params(JobPriority.HIGH)
        .groupBy(JobGroup.PHOTOCARD)
        .addTags(TAG)
        .requireNetwork()
        .persist()) {

    companion object {
        val TAG = "PhotocardCreateJob"
    }

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
                    dataManager.createPhotocard(photocard)
                }
                .doOnNext {
                    if (photocard.views > 0) {
                        it.views = photocard.views
                        it.sync = false
                    }
                    val album = dataManager.getDetachedObjFromDb(Album::class.java, photocard.album)!!
                    album.photocards.add(it)
                    dataManager.save(album)
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
    }

    private fun removeTempPhotocard() {
        DaggerService.appComponent.dataManager().removeFromDb(Photocard::class.java, photocardId)
        DaggerService.appComponent.cache().removePhoto(photocardId)
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int) =
            cancelOrWaitConnection(throwable, runCount)
}

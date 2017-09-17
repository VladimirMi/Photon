package io.github.vladimirmi.photon.data.jobs.photocard

import android.net.Uri
import io.github.vladimirmi.photon.data.jobs.ChainJob
import io.github.vladimirmi.photon.data.jobs.album.AlbumCreateJob
import io.github.vladimirmi.photon.data.managers.utils.JobGroup
import io.github.vladimirmi.photon.di.DaggerService
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody

/**
 * Created by Vladimir Mikhalev 25.06.2017.
 */

class PhotocardCreateJob(private val photocardId: String)
    : ChainJob(TAG, JobGroup.PHOTOCARD, photocardId) {

    companion object {
        val TAG = "PhotocardCreateJob"
    }

    private val albumId = DaggerService.appComponent.photocardJobRepository().getPhotocard(photocardId).album
    override val needCreate = listOf(AlbumCreateJob.TAG + albumId)

    override fun onStart() {
        val repository = DaggerService.appComponent.photocardJobRepository()
        val photocard = repository.getPhotocard(photocardId)

        val data = getByteArrayFromContent(photocard.photo)
        val body = RequestBody.create(MediaType.parse("multipart/form-data"), data)
        val bodyPart = MultipartBody.Part.createFormData("image", Uri.parse(photocard.photo).lastPathSegment, body)

        val photocardRes = repository.uploadPhoto(bodyPart)
                .flatMap { imageUrlRes ->
                    photocard.photo = imageUrlRes.image
                    result?.let { photocard.album = it }
                    repository.create(photocard)
                }.blockingGet()

        photocard.id = photocardRes.id
        result = photocard.id
        repository.save(photocard)
    }

    override fun onError(throwable: Throwable) {}

    private fun getByteArrayFromContent(contentUri: String): ByteArray {
        DaggerService.appComponent.context().contentResolver
                .openInputStream(Uri.parse(contentUri)).use {
            return it.readBytes()
        }
    }

    override fun copy() = PhotocardCreateJob(photocardId).apply {
        queue.addAll(this@PhotocardCreateJob.queue)
    }
}

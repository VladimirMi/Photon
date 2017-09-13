package io.github.vladimirmi.photon.data.jobs.photocard

import android.net.Uri
import io.github.vladimirmi.photon.data.jobs.ChainJob
import io.github.vladimirmi.photon.data.repository.photocard.PhotocardJobRepository
import io.github.vladimirmi.photon.di.DaggerService
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody

/**
 * Created by Vladimir Mikhalev 25.06.2017.
 */

class PhotocardCreateJob(private val photocardId: String,
                         private val repository: PhotocardJobRepository)
    : ChainJob(TAG, photocardId) {

    companion object {
        val TAG = "PhotocardCreateJob"
    }

    override fun onRun() {
        val photocard = repository.getPhotocard(photocardId)

        val data = getByteArrayFromContent(photocard.photo)
        val body = RequestBody.create(MediaType.parse("multipart/form-data"), data)
        val bodyPart = MultipartBody.Part.createFormData("image", Uri.parse(photocard.photo).lastPathSegment, body)

        val photocardRes = repository.uploadPhoto(bodyPart)
                .flatMap { imageUrlRes ->
                    photocard.photo = imageUrlRes.image
                    repository.create(photocard)
                }.blockingGet()

        photocard.id = photocardRes.id
        repository.save(photocard)
    }

    private fun getByteArrayFromContent(contentUri: String): ByteArray {
        DaggerService.appComponent.context().contentResolver
                .openInputStream(Uri.parse(contentUri)).use {
            return it.readBytes()
        }
    }
}

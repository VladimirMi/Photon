package io.github.vladimirmi.photon.features.newcard

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.OpenableColumns
import flow.Flow
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.features.album.AlbumScreen
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.github.vladimirmi.photon.flow.BottomNavHistory.BottomItem.PROFILE
import io.github.vladimirmi.photon.utils.AppConfig
import io.github.vladimirmi.photon.utils.Constants
import io.github.vladimirmi.photon.utils.ErrorObserver
import io.github.vladimirmi.photon.utils.JobStatus
import timber.log.Timber
import java.io.File


class NewCardPresenter(model: INewCardModel, rootPresenter: RootPresenter)
    : BasePresenter<NewCardView, INewCardModel>(model, rootPresenter) {

    private var returnTo: AlbumDto? = null
    private var startAction: (() -> Unit)? = null

    override fun initToolbar() {
        rootPresenter.getNewToolbarBuilder()
                .build()
    }

    override fun initView(view: NewCardView) {
        startAction?.invoke()

//        chooseWhatShow()
        view.showPhotoParams()
        Timber.e("initView: ${model.screenInfo.currentPage}")
        view.changePage(model.screenInfo.currentPage)
    }

    private fun chooseWhatShow() {
        if (model.screenInfo.photo.isNotEmpty()) {
            view.showPhotoParams()
        } else {
            view.showPhotoChoose()
        }
    }

    fun onBackPressed(): Boolean {
        if (model.screenInfo.photo.isNotEmpty()) {
            clearPhotocard()
            return true
        }
        if (returnTo != null) {
            returnToAlbum()
            return true
        }
        return false
    }

    fun returnToAlbum() {
        Flow.getKey<NewCardScreen>(view)?.album = null
        val history = rootPresenter.bottomHistory.historyMap[PROFILE]
        val newHistory = history!!.buildUpon().apply {
            pop()
            returnTo?.let { push(AlbumScreen(it)) }
        }.build()
        rootPresenter.bottomHistory.historyMap[PROFILE] = newHistory
        rootPresenter.navigateTo(PROFILE)
    }

    fun savePhotocard() {
        if (!checkCanSave()) return
        compDisp.add(model.uploadPhotocard()
                .subscribeWith(object : ErrorObserver<JobStatus>() {

                    override fun onNext(it: JobStatus) {
                        if (returnTo != null) returnToAlbum() else clearPhotocard()
                    }

                    override fun onComplete() {
                        view.showError(R.string.newcard_create_success)
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        view.showError(R.string.message_api_err_unknown)
                    }
                }))
    }

    private fun checkCanSave(): Boolean {
        fun pageIsValid(page: Page): Boolean {
            return model.getPageError(page)?.let { errorId ->
                view.changePage(page)
                view.showError(errorId)
                false
            } ?: true
        }

        if (!pageIsValid(model.screenInfo.currentPage)) return false
        return Page.values().any { pageIsValid(it) }
    }

    fun clearPhotocard() {
        model.screenInfo = NewCardScreenInfo()
        view.clearView()
        chooseWhatShow()
    }

    fun choosePhoto() {
        val permissions = arrayOf(READ_EXTERNAL_STORAGE)
        if (rootPresenter.checkAndRequestPermissions(permissions, Constants.REQUEST_GALLERY)) {
            takePhoto()
        }
    }

    private fun takePhoto() {
        val intent = Intent()
        intent.type = Constants.MIME_TYPE_IMAGE
        intent.action = Intent.ACTION_GET_CONTENT
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        rootPresenter.startActivityForResult(intent, Constants.REQUEST_GALLERY)
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        val requestCanceled = grantResults.contains(PackageManager.PERMISSION_DENIED) || grantResults.isEmpty()
        if (requestCanceled) {
            rootPresenter.showPermissionSnackBar()
        } else if (requestCode == Constants.REQUEST_GALLERY) {
            takePhoto()
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.REQUEST_GALLERY && data != null && data.data != null) {
                checkMetaAndSave(data.data)
            }
        }
    }

    private fun checkMetaAndSave(uri: Uri) {
        val limit = AppConfig.IMAGE_SIZE_LIMIT * 1024 * 1024 // bytes
        val fileSize = when (uri.scheme) {
            "file" -> File(uri.path).length().toInt()
            else -> {
                DaggerService.appComponent.context()
                        .contentResolver.query(uri, null, null, null, null, null).use { cursor ->
                    if (cursor != null && cursor.moveToFirst()) {
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (!cursor.isNull(sizeIndex)) cursor.getInt(sizeIndex) else null
                    } else null
                }
            }
        }

        if (fileSize != null && fileSize < limit) {
            model.screenInfo.photo = uri.toString()
            startAction = null
        } else {
            startAction = { view.showError(R.string.message_err_file_size, AppConfig.IMAGE_SIZE_LIMIT) }
        }
    }

    fun saveCurrentPage(page: Page) {
        Timber.e("saveCurrentPage: $page")
        model.screenInfo.currentPage = page
    }
}


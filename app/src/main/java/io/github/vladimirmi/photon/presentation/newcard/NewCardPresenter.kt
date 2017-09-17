package io.github.vladimirmi.photon.presentation.newcard

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.OpenableColumns
import flow.Flow
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.data.managers.utils.JobStatus
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.flow.BottomNavigationHistory.BottomItem.PROFILE
import io.github.vladimirmi.photon.presentation.album.AlbumScreen
import io.github.vladimirmi.photon.presentation.root.RootPresenter
import io.github.vladimirmi.photon.utils.AppConfig
import io.github.vladimirmi.photon.utils.Constants
import io.github.vladimirmi.photon.utils.ErrorObserver
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@DaggerScope(NewCardScreen::class)
class NewCardPresenter
@Inject constructor(model: NewCardInteractor, rootPresenter: RootPresenter)
    : BasePresenter<NewCardView, NewCardInteractor>(model, rootPresenter) {

    private var startAction: (() -> Unit)? = null

    override fun initToolbar() {
        rootPresenter.getNewToolbarBuilder()
                .build()
    }

    override fun initView(view: NewCardView) {
        model.screenInfo = Flow.getKey<NewCardScreen>(view.context)!!.info
        startAction?.invoke()
        chooseWhatShow()
        view.changePage(model.screenInfo.currentPage)
    }

    override fun dropView(view: NewCardView) {
        Flow.getKey<NewCardScreen>(view)?.info = model.screenInfo
        super.dropView(view)
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
        if (model.screenInfo.returnToAlbum) {
            returnToAlbum()
            return true
        }
        return false
    }

    fun returnToAlbum() {
        val history = rootPresenter.bottomHistory.historyMap[PROFILE]
        val newHistory = history!!.buildUpon().apply {
            pop()
            push(AlbumScreen(model.screenInfo.album))
        }.build()
        rootPresenter.bottomHistory.historyMap[PROFILE] = newHistory
        rootPresenter.navigateTo(PROFILE)
    }

    fun savePhotocard() {
        if (!checkCanSave()) return
        compDisp.add(model.uploadPhotocard()
                .subscribeWith(object : ErrorObserver<JobStatus>() {

                    override fun onNext(it: JobStatus) {
                        if (model.screenInfo.returnToAlbum) returnToAlbum() else clearPhotocard()
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
                return false
            } ?: true
        }

        fun pageIsLast(page: Page, returnToAlbum: Boolean): Boolean {
            val isLast = if (returnToAlbum && page == Page.PARAMS) true
            else !returnToAlbum && page == Page.ALBUMS

            if (!isLast) view.changePage(Page.fromIndex(page.index + 1))
            return isLast
        }
        return pageIsValid(model.screenInfo.currentPage) &&
                Page.values().all { pageIsValid(it) } &&
                pageIsLast(model.screenInfo.currentPage, model.screenInfo.returnToAlbum)
    }

    fun clearPhotocard() {
        Flow.getKey<NewCardScreen>(view)?.info = NewCardScreenInfo()
        initView(view)
//        view.clearView()
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


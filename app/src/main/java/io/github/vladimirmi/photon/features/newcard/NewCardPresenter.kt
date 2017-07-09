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
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.models.realm.Tag
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.github.vladimirmi.photon.flow.BottomNavHistory.BottomItem.PROFILE
import io.github.vladimirmi.photon.utils.AppConfig
import io.github.vladimirmi.photon.utils.Constants
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit


class NewCardPresenter(model: INewCardModel, rootPresenter: RootPresenter)
    : BasePresenter<NewCardView, INewCardModel>(model, rootPresenter) {

    private var returnToProfile: Boolean = false
    private var startAction: (() -> Unit)? = null

    override fun initToolbar() {
        rootPresenter.getNewToolbarBuilder().build()
    }

    override fun initView(view: NewCardView) {
        startAction?.invoke()
        Flow.getKey<NewCardScreen>(view)?.albumId?.let {
            returnToProfile = true
            setAlbumId(it)
        }
        compDisp.add(subscribeInTitleField())
        compDisp.add(subscribeOnTagField())
        view.setTags(model.photoCard.tags)
        compDisp.add(subscribeOnAlbums())

        chooseWhatShow()
    }

    private fun chooseWhatShow() {
        if (model.photoCard.photo.isNotEmpty()) {
            view.showPhotoParams()
        } else {
            view.showPhotoChoose()
        }
    }

    fun onBackPressed(): Boolean {
        if (model.photoCard.photo.isNotEmpty()) {
            clearPhotocard()
            return true
        }
        if (returnToProfile) {
            Flow.getKey<NewCardScreen>(view)?.albumId = null
            rootPresenter.navigateTo(PROFILE)
            return true
        }
        return false
    }

    private fun subscribeInTitleField(): Disposable {
        return view.nameObs
                .debounce(600, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe { model.photoCard.title = it.toString() }
    }

    private fun subscribeOnTagField(): Disposable {
        return view.tagObs.doOnNext { view.setTagActionIcon(it.isNotEmpty()) }
                .debounce(600, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .flatMap { model.search(it.toString()) }
                .subscribe { view.setTagSuggestions(it) }
    }

    private fun subscribeOnAlbums(): Disposable {
        return model.getAlbums()
                .subscribe {
                    view.setAlbums(it)
                }
    }

    fun addFilter(filter: Pair<String, String>) = model.addFilter(filter)

    fun removeFilter(filter: Pair<String, String>) = model.removeFilter(filter)

    fun saveTag(tag: String) {
        model.addTag(Tag(tag))
        view.setTags(model.photoCard.tags)
    }

    fun setAlbumId(albumId: String) {
        model.photoCard.album = albumId
        view.selectAlbum(albumId)
    }

    fun savePhotocard() {
        model.uploadPhotocard()
        clearPhotocard()
    }

    fun clearPhotocard() {
        model.photoCard = Photocard()
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
        DaggerService.appComponent.context().contentResolver
                .query(uri, null, null, null, null, null).use { cursor ->
            if (cursor != null && cursor.moveToFirst()) {
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                val size = if (!cursor.isNull(sizeIndex)) cursor.getInt(sizeIndex) else null

                val limit = AppConfig.IMAGE_SIZE_LIMIT * 1024 * 1024 // bytes
                if (size != null && size < limit) {
                    model.savePhotoUri(uri.toString())
                    startAction = null
                } else {
                    startAction = { view.showError(R.string.message_err_file_size, AppConfig.IMAGE_SIZE_LIMIT) }
                }
            }
        }

    }
}


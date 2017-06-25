package io.github.vladimirmi.photon.features.newcard

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.data.models.Album
import io.github.vladimirmi.photon.data.models.Tag
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.github.vladimirmi.photon.utils.Constants
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class NewCardPresenter(model: INewCardModel, rootPresenter: RootPresenter)
    : BasePresenter<NewCardView, INewCardModel>(model, rootPresenter) {

    override fun initToolbar() {
        rootPresenter.getNewToolbarBuilder().build()
    }

    override fun initView(view: NewCardView) {
        if (model.photoCard.photo.isNotEmpty()) view.showPhotoParams()
        compDisp.add(subscribeOnTagField())
        view.setTags(model.photoCard.tags)
        compDisp.add(subscribeOnAlbums())
    }

    private fun subscribeOnTagField(): Disposable {
        return view.tagObs.doOnNext { view.setTagActionIcon(it.isNotEmpty()) }
                .debounce(600, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .flatMap { model.search(it.toString()) }
                .subscribe { view.setTagSuggestions(it) }
    }

    private fun subscribeOnAlbums(): Disposable {
        return model.getAlbums()
                .subscribe { view.setAlbums(it) }
    }

    fun addFilter(filter: Pair<String, String>) {
        model.addFilter(filter)
    }

    fun removeFilter(filter: Pair<String, String>) {
        model.removeFilter(filter)
    }

    fun saveTag(tag: String) {
        model.addTag(Tag(tag))
        view.setTags(model.photoCard.tags)
    }

    fun setAlbum(album: Album) {
        view.selectAlbum(album)
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
        intent.action = Intent.ACTION_OPEN_DOCUMENT
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        rootPresenter.startActivityForResult(intent, Constants.REQUEST_GALLERY)
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        var requestCanceled = false
        for (grantResult in grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                requestCanceled = true
                break
            }
        }
        if (grantResults.isEmpty()) requestCanceled = true
        if (requestCanceled) {
            rootPresenter.showPermissionSnackBar()
        } else if (requestCode == Constants.REQUEST_GALLERY) {
            takePhoto()
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.REQUEST_GALLERY && data != null && data.data != null) {
                model.savePhotoUri(data.data.toString())
            }
        }
    }
}


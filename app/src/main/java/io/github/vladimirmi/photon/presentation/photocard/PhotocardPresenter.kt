package io.github.vladimirmi.photon.presentation.photocard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v4.content.FileProvider
import android.view.MenuItem
import flow.Flow
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.domain.models.PhotocardDto
import io.github.vladimirmi.photon.presentation.author.AuthorScreen
import io.github.vladimirmi.photon.presentation.root.MenuItemHolder
import io.github.vladimirmi.photon.presentation.root.RootPresenter
import io.github.vladimirmi.photon.utils.*
import io.reactivex.disposables.Disposable
import java.io.File
import javax.inject.Inject

@DaggerScope(PhotocardScreen::class)
class PhotocardPresenter
@Inject constructor(model: PhotocardInteractor, rootPresenter: RootPresenter) :
        BasePresenter<PhotocardView, PhotocardInteractor>(model, rootPresenter) {

    private val actions: (MenuItem) -> Unit = {
        when (it.itemId) {
            R.id.menu_favorite -> rootPresenter.afterAuthCheck(view) { addToFavorite() }
            R.id.menu_favorite_remove -> rootPresenter.afterAuthCheck(view) { removeFromFavorite() }
            R.id.menu_share -> share()
            R.id.menu_download -> rootPresenter.afterNetCheck(view) { download() }
        }
    }
    private var isFavorite = false

    override fun initToolbar() {
        val popup = if (rootPresenter.isUserAuth()) {
            if (isFavorite) R.menu.submenu_photocard_screen_fav else R.menu.submenu_photocard_screen
        } else {
            R.menu.submenu_photocard_screen_not_auth
        }

        rootPresenter.getNewToolbarBuilder()
                .setToolbarTitleId(R.string.photocard_title)
                .setBackNavigationEnabled(true)
                .addAction(MenuItemHolder("Actions",
                        iconResId = R.drawable.ic_action_more,
                        popupMenu = popup,
                        actions = actions))
                .build()
    }

    private lateinit var photocard: PhotocardDto

    override fun initView(view: PhotocardView) {
        val id = Flow.getKey<PhotocardScreen>(view)?.photocardId!!
        val ownerId = Flow.getKey<PhotocardScreen>(view)?.ownerId!!
        compDisp.add(subscribeOnUser(ownerId))
        compDisp.add(subscribeOnPhotocard(id))
        compDisp.add(subscribeOnIsFavorite(id))
    }

    private fun subscribeOnIsFavorite(id: String): Disposable {
        return model.isFavorite(id).subscribe { favorite ->
            if (isFavorite != favorite) {
                isFavorite = favorite
                initToolbar()
            }
            view.setFavorite(isFavorite)
        }
    }

    private fun subscribeOnUser(ownerId: String): Disposable {
        return model.getUser(ownerId)
                .doOnNext { view.setUser(it) }
                .subscribeWith(ErrorObserver())
    }

    private fun subscribeOnPhotocard(id: String): Disposable {
        return model.getPhotocard(id)
                .doOnNext {
                    photocard = it
                    view.setPhotocard(it)
                }
                .subscribeWith(ErrorObserver())
    }


    fun showAuthor() {
        rootPresenter.afterNetCheck(view) { Flow.get(view).set(AuthorScreen(photocard.owner)) }
    }

    private fun addToFavorite() {
        compDisp.add(model.addToFavorite(photocard.id)
                .subscribeWith(ErrorObserver(view)))
    }

    private fun removeFromFavorite() {
        compDisp.add(model.removeFromFavorite(photocard.id)
                .subscribeWith(ErrorObserver(view)))
    }

    private var tempFile: File? = null

    private fun share() {
        if (rootPresenter.isNetAvailable()) {
            tempFile = createTempFile(suffix = ".jpg", directory = view.context.cacheDir)
            photocard.downloadTo(tempFile!!, view.context)
                    .subscribeWith(object : ErrorSingleObserver<Unit>() {
                        override fun onSuccess(it: Unit) {
                            rootPresenter.startActivityForResult(createShareImageIntent(), Constants.REQUEST_SHARE)
                        }
                    })
        } else {
            rootPresenter.startActivityForResult(createShareLinkIntent(), Constants.REQUEST_SHARE)
        }
    }

    private fun createShareImageIntent(): Intent {
        val uri = FileProvider.getUriForFile(view.context,
                AppConfig.FILE_PROVIDER_AUTHORITY, tempFile)

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "image/jpeg"
        }

        return Intent.createChooser(shareIntent, view.resources.getText(R.string.send_to))
    }

    private fun createShareLinkIntent(): Intent {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, photocard.photo)
            type = "text/plain"
        }
        return Intent.createChooser(shareIntent, view.resources.getText(R.string.send_to))
    }

    private fun download() {
        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (rootPresenter.checkAndRequestPermissions(permissions, Constants.REQUEST_WRITE)) {
            downloadPhoto()
        }
    }

    private fun downloadPhoto() {
        val file = rootPresenter.createFileForPhotocard(photocard)
        if (file != null) {
            photocard.downloadTo(file, view.context)
                    .subscribeWith(object : ErrorSingleObserver<Unit>() {
                        override fun onSuccess(it: Unit) {
                            view.showLoadSnackbar { showLoadedPhoto(file) }
                        }
                    })
        } else {
            view.showMessage(R.string.message_err_create_file)
        }
    }

    fun showLoadedPhoto(file: File) {
        val uri = FileProvider.getUriForFile(view.context,
                AppConfig.FILE_PROVIDER_AUTHORITY, file)
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            setDataAndType(uri, "image/jpeg")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        rootPresenter.startActivity(intent)
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        val requestCanceled = grantResults.contains(PackageManager.PERMISSION_DENIED) || grantResults.isEmpty()
        if (requestCanceled) {
            rootPresenter.showPermissionSnackBar()
        } else if (requestCode == Constants.REQUEST_WRITE) {
            downloadPhoto()
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.REQUEST_SHARE) {
            tempFile?.run {
                delete()
                tempFile = null
            }
        }
    }
}
package io.github.vladimirmi.photon.features.photocard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Environment
import android.support.v4.content.FileProvider
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import flow.Flow
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.features.author.AuthorScreen
import io.github.vladimirmi.photon.features.root.MenuItemHolder
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.github.vladimirmi.photon.utils.AppConfig
import io.github.vladimirmi.photon.utils.Constants
import io.github.vladimirmi.photon.utils.ErrorObserver
import io.github.vladimirmi.photon.utils.ioToMain
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.io.File


class PhotocardPresenter(model: IPhotocardModel, rootPresenter: RootPresenter) :
        BasePresenter<PhotocardView, IPhotocardModel>(model, rootPresenter) {

    private val actions: (MenuItem) -> Unit = {
        when (it.itemId) {
            R.id.menu_favorite -> addToFavorite()
            R.id.menu_favorite_remove -> removeFromFavorite()
            R.id.menu_share -> share()
            R.id.menu_download -> download()
        }
    }
    private var isFavorite = false

    override fun initToolbar() {
        var popup = if (rootPresenter.isUserAuth()) {
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

    lateinit var photocard: Photocard

    override fun initView(view: PhotocardView) {
        val photocard = Flow.getKey<PhotocardScreen>(view)?.photocard!!
        this.photocard = photocard
        compDisp.add(subscribeOnUser(photocard.owner))
        compDisp.add(subscribeOnPhotocard(photocard))
        compDisp.add(subscribeOnIsFavorite(photocard))
    }

    private fun subscribeOnIsFavorite(photocard: Photocard): Disposable {
        return model.isFavorite(photocard).subscribe { favorite ->
            if (isFavorite != favorite) {
                isFavorite = favorite
                initToolbar()
            }
            view.setFavorite(isFavorite)

        }
    }

    private fun subscribeOnUser(owner: String): Disposable {
        return model.getUser(owner)
                .subscribeWith(object : ErrorObserver<User>() {
                    override fun onNext(it: User) {
                        view.setUser(it)
                    }
                })
    }

    private fun subscribeOnPhotocard(photocard: Photocard): Disposable {
        return Observable.just(photocard)
                .mergeWith(model.getPhotocard(photocard.id, photocard.owner))
                .subscribeWith(object : ErrorObserver<Photocard>() {
                    override fun onNext(it: Photocard) {
                        view.setPhotocard(it)
                    }
                })
    }

    fun showAuthor() {
        Flow.get(view).set(AuthorScreen(photocard.owner))
    }

    private fun addToFavorite() {
        if (rootPresenter.isUserAuth()) {
            compDisp.add(model.addToFavorite(photocard).subscribeWith(ErrorObserver()))
        }
    }

    private fun removeFromFavorite() {
        if (rootPresenter.isUserAuth()) {
            compDisp.add(model.removeFromFavorite(photocard).subscribeWith(ErrorObserver()))
        }
    }

    private var tempFile: File? = null

    private fun share() {
        tempFile = createTempFile(suffix = ".jpg", directory = view.context.cacheDir)
        val uri = FileProvider.getUriForFile(view.context, AppConfig.FILE_PROVIDER_AUTHORITY, tempFile)

        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        shareIntent.type = "image/jpeg"

        downloadTo(tempFile as File) {
            rootPresenter.startActivityForResult(Intent.createChooser(shareIntent, view.resources.getText(R.string.send_to)),
                    Constants.REQUEST_SHARE)
        }
    }

    private fun download() {
        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (rootPresenter.checkAndRequestPermissions(permissions, Constants.REQUEST_WRITE)) {
            downloadPhoto()
        }
    }

    private fun downloadPhoto() {
        val file = createFile()
        if (file != null) {
            downloadTo(file) {
                view.showMessage(R.string.photocard_message_download)
            }
        } else {
            view.showMessage("Неудалось создать файл") //todo
        }
    }

    private fun downloadTo(file: File, doneCallback: () -> Unit) {
        Glide.with(view.context)
                .load(photocard.photo)
                .asBitmap()
                .toBytes(Bitmap.CompressFormat.JPEG, 100)
                .into(object : SimpleTarget<ByteArray>() {
                    override fun onResourceReady(resource: ByteArray, glideAnimation: GlideAnimation<in ByteArray>) {
                        Observable.just(resource)
                                .flatMap { Observable.just(file.writeBytes(it)) }
                                .ioToMain()
                                .subscribeWith(object : ErrorObserver<Any>() {
                                    override fun onNext(it: Any) {
                                        doneCallback()
                                    }
                                })
                    }
                })
    }

    private fun createFile(): File? {
        val folder = Environment.getExternalStorageDirectory()
        val file = File(folder, "/Photon/${photocard.title}.jpg")
        val dir = file.parentFile
        if (!dir.mkdirs() && (!dir.exists() || !dir.isDirectory)) {
            return null
        }
        return file
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
            if (tempFile != null) {
                Timber.e("onActivityResult: delete file")
                tempFile?.delete()
                tempFile = null
            }
        }
    }
}
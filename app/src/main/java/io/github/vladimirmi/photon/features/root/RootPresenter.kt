package io.github.vladimirmi.photon.features.root

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import io.github.vladimirmi.photon.data.models.dto.PhotocardDto
import io.github.vladimirmi.photon.data.models.req.SignInReq
import io.github.vladimirmi.photon.data.models.req.SignUpReq
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.flow.BottomNavHistory
import io.github.vladimirmi.photon.utils.ErrorCompletableObserver
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import mortar.MortarScope
import mortar.Presenter
import mortar.bundler.BundleService
import java.io.File

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

@DaggerScope(RootActivity::class)
class RootPresenter(val model: IRootModel) :
        Presenter<IRootView>() {

    val bottomHistory = BottomNavHistory()
    private var syncDB: Disposable? = null

    override fun extractBundleService(view: IRootView?): BundleService =
            BundleService.getBundleService(view as Context)

    override fun onEnterScope(scope: MortarScope?) {
        if (isUserAuth()) syncDB()
    }

    override fun onExitScope() {
        syncDB?.dispose()
    }

    private fun syncDB() {
        syncDB = model.syncProfile().subscribeWith(ErrorCompletableObserver())
    }

    fun hasActiveView() = hasView()

    fun getNewToolbarBuilder(): ToolbarBuilder = ToolbarBuilder(view)

    fun isUserAuth(): Boolean = model.isUserAuth()

    fun showLoading() = view.showLoading()

    fun hideLoading() = view.hideLoading()

    fun register(req: SignUpReq): Completable {
        return model.register(req)
                .doOnComplete { syncDB() }
                .doOnSubscribe { showLoading() }
                .doAfterTerminate { hideLoading() }
    }

    fun login(req: SignInReq): Completable {
        return model.login(req)
                .doOnComplete { syncDB() }
                .doOnSubscribe { showLoading() }
                .doAfterTerminate { hideLoading() }
    }

    fun logout() {
        syncDB?.dispose()
        model.logout()
    }


    fun checkAndRequestPermissions(permissions: Array<String>, requestCode: Int): Boolean {
        if (!hasView()) return false
        val allGranted = permissions.none {
            ContextCompat.checkSelfPermission(view as RootActivity, it) == PackageManager.PERMISSION_DENIED
        }
        if (!allGranted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                (view as RootActivity).requestPermissions(permissions, requestCode)
            } else {
                ActivityCompat.requestPermissions(view as RootActivity, permissions, requestCode)
            }
        }
        return allGranted
    }

    fun createFileForPhotocard(photocard: PhotocardDto): File? {
        val folder = Environment.getDataDirectory()
        val file = File(folder, "/Photon/${photocard.title}.jpg")
        val dir = file.parentFile
        if (!dir.mkdirs() && (!dir.exists() || !dir.isDirectory)) {
            return null
        }
        return file
    }

    fun startActivityForResult(intent: Intent, requestCode: Int) = (view as RootActivity).startActivityForResult(intent, requestCode)

    fun startActivity(intent: Intent) = (view as RootActivity).startActivity(intent)

    fun showPermissionSnackBar() = view.showPermissionSnackBar()

    fun navigateTo(bottomItem: BottomNavHistory.BottomItem) = view.navigateTo(bottomItem)

    fun showMessage(stringId: Int) = view.showMessage(stringId)

    fun clearMenu() = view.clearToolbar()

    fun isNetAvailable() = model.isNetAvail()
}



package io.github.vladimirmi.photon.features.root

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import io.github.vladimirmi.photon.core.IView
import io.github.vladimirmi.photon.data.models.dto.PhotocardDto
import io.github.vladimirmi.photon.data.models.req.SignInReq
import io.github.vladimirmi.photon.data.models.req.SignUpReq
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.flow.BottomNavigationHistory
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import mortar.Presenter
import mortar.bundler.BundleService
import java.io.File

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

@DaggerScope(RootActivity::class)
class RootPresenter(val model: IRootModel) :
        Presenter<IRootView>() {

    val bottomHistory = BottomNavigationHistory(authMode = !isUserAuth())
    private var syncDB: Disposable? = null

    override fun extractBundleService(view: IRootView?): BundleService =
            BundleService.getBundleService(view as Context)

    fun hasActiveView() = hasView()

    fun getNewToolbarBuilder(): ToolbarBuilder = ToolbarBuilder(view)

    fun isUserAuth(): Boolean = model.isUserAuth()

    fun showLoading() = view.showLoading()

    fun hideLoading() = view.hideLoading()

    fun register(req: SignUpReq): Completable =
            model.register(req)
                    .doOnSubscribe { showLoading() }
                    .doOnComplete { endAuthorization() }

    private fun endAuthorization() {
        hideLoading()
        bottomHistory.authMode = false
    }

    fun login(req: SignInReq): Completable =
            model.login(req)
                    .doOnSubscribe { showLoading() }
                    .doOnComplete { endAuthorization() }

    fun logout() {
        syncDB?.dispose()
        model.logout()
        bottomHistory.authMode = true
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

    fun clearMenu() = view.clearToolbar()

    fun navigateTo(bottomItem: BottomNavigationHistory.BottomItem) = view.navigateTo(bottomItem)

    inline fun <V : IView> afterNetCheck(view: V, block: V.() -> Unit) {
        if (model.isNetAvail()) block(view) else view.showNetError()
    }

    inline fun <V : IView> afterAuthCheck(view: V, block: V.() -> Unit) {
        if (model.isUserAuth()) block(view) else view.showAuthError()
    }

    fun isNetAvailable() = model.isNetAvail()
}



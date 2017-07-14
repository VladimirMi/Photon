package io.github.vladimirmi.photon.features.root

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import io.github.vladimirmi.photon.data.models.SignInReq
import io.github.vladimirmi.photon.data.models.SignUpReq
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.flow.BottomNavHistory
import io.reactivex.Observable
import mortar.Presenter
import mortar.bundler.BundleService

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

@DaggerScope(RootActivity::class)
class RootPresenter(val model: IRootModel) :
        Presenter<IRootView>() {

    var bottomHistory: BottomNavHistory? = null

    override fun extractBundleService(view: IRootView?): BundleService {
        return BundleService.getBundleService(view as Context)
    }

    fun hasActiveView() = hasView()

    fun getNewToolbarBuilder(): ToolbarBuilder = ToolbarBuilder(view)

    fun isUserAuth(): Boolean {
        return model.isUserAuth()
    }

    fun showLoading() {
        view.showLoading()
    }

    fun hideLoading() {
        view.hideLoading()
    }

    fun register(req: SignUpReq): Observable<User> {
        return model.register(req)
                .doOnSubscribe { showLoading() }
                .doAfterTerminate { hideLoading() }
    }

    fun login(req: SignInReq): Observable<User> {
        return model.login(req)
                .doOnSubscribe { showLoading() }
                .doAfterTerminate { hideLoading() }
    }

    fun logout() = model.logout()


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

    fun startActivityForResult(intent: Intent, requestCode: Int) {
        (view as RootActivity).startActivityForResult(intent, requestCode)
    }

    fun startActivity(intent: Intent) {
        (view as RootActivity).startActivity(intent)
    }

    fun showPermissionSnackBar() {
        view.showPermissionSnackBar()
    }

    fun navigateTo(bottomItem: BottomNavHistory.BottomItem) {
        view.navigateTo(bottomItem)
    }

    fun showMessage(stringId: Int) {
        view.showMessage(stringId)
    }

    fun clearMenu() {
        view.clearToolbar()
    }
}



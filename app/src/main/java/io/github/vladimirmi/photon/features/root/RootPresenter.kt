package io.github.vladimirmi.photon.features.root

import android.content.Context

import io.github.vladimirmi.photon.data.models.SignInReq
import io.github.vladimirmi.photon.data.models.SignUpReq
import io.github.vladimirmi.photon.data.models.User
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.flow.BottomNavDispatcher
import io.reactivex.Observable
import mortar.Presenter
import mortar.bundler.BundleService
/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

@DaggerScope(RootActivity::class)
class RootPresenter(val model: IRootModel) :
        Presenter<IRootView>() {

    var bottomNavigator: BottomNavDispatcher? = null

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
}



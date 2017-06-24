package io.github.vladimirmi.photon.features.auth

import flow.Direction
import flow.Flow
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.data.models.SignInReq
import io.github.vladimirmi.photon.data.models.SignUpReq
import io.github.vladimirmi.photon.data.network.ApiError
import io.github.vladimirmi.photon.features.main.MainScreen
import io.github.vladimirmi.photon.features.newcard.NewCardScreen
import io.github.vladimirmi.photon.features.profile.ProfileScreen
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.github.vladimirmi.photon.flow.BottomNavDispatcher

/**
 * Created by Vladimir Mikhalev 25.06.2017.
 */

class AuthPresenter(model: IAuthModel, rootPresenter: RootPresenter)
    : BasePresenter<AuthView, IAuthModel>(model, rootPresenter) {
    override fun initToolbar() {
        rootPresenter.getNewToolbarBuilder().build()
    }

    override fun initView(view: AuthView) {

    }

    fun register(req: SignUpReq) {
        compDisp.add(rootPresenter.register(req)
                .subscribe({}, {
                    // onError
                    if (it is ApiError) view.showMessage(it.errorResId)
                }, {
                    //onComplete
                    view.closeRegistrationDialog()
                    nextScreen()
                }))
    }

    fun login(req: SignInReq) {
        compDisp.add(rootPresenter.login(req)
                .doOnSubscribe { rootPresenter.showLoading() }
                .doAfterTerminate { rootPresenter.hideLoading() }
                .subscribe({}, {
                    // onError
                    if (it is ApiError) {
                        when (it.statusCode) {
                            404 -> view.showMessage(R.string.message_api_err_auth)
                            else -> view.showMessage(it.errorResId)
                        }
                    }
                }, {
                    //onComplete
                    view.closeLoginDialog()
                    nextScreen()
                }))
    }

    private fun nextScreen() {
        val nextScreen = when (rootPresenter.bottomNavigator!!.currentItem) {
            BottomNavDispatcher.BottomItem.PROFILE -> ProfileScreen()
            BottomNavDispatcher.BottomItem.LOAD -> NewCardScreen()
            BottomNavDispatcher.BottomItem.MAIN -> MainScreen()
        }
        Flow.get(view).replaceTop(nextScreen, Direction.REPLACE)
    }
}
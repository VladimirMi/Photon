package io.github.vladimirmi.photon.features.auth

import flow.Direction
import flow.Flow
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.data.models.req.SignInReq
import io.github.vladimirmi.photon.data.models.req.SignUpReq
import io.github.vladimirmi.photon.data.network.ApiError
import io.github.vladimirmi.photon.features.main.MainScreen
import io.github.vladimirmi.photon.features.newcard.NewCardScreen
import io.github.vladimirmi.photon.features.profile.ProfileScreen
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.github.vladimirmi.photon.flow.BottomNavHistory
import io.github.vladimirmi.photon.utils.ErrorSingleObserver

/**
 * Created by Vladimir Mikhalev 25.06.2017.
 */

class AuthPresenter(model: IAuthModel, rootPresenter: RootPresenter)
    : BasePresenter<AuthView, IAuthModel>(model, rootPresenter) {

    override fun initToolbar() {
        rootPresenter.getNewToolbarBuilder().build()
    }

    override fun initView(view: AuthView) {
        when (rootPresenter.bottomHistory.currentItem) {
            BottomNavHistory.BottomItem.PROFILE -> view.setTitle(R.string.profile_not_auth)
            BottomNavHistory.BottomItem.LOAD -> view.setTitle(R.string.newcard_not_auth)
            else -> {
            }
        }
    }

    fun register(req: SignUpReq) {
        compDisp.add(rootPresenter.register(req)
                .doOnSubscribe { view.closeRegistrationDialog() }
                .subscribeWith(object : ErrorSingleObserver<Unit>() {
                    override fun onSuccess(t: Unit) {
                        nextScreen()
                    }

                    override fun onError(e: Throwable) {
                        if (e is ApiError) {
                            view.showError(e.errorResId)
                        } else {
                            view.showError(R.string.message_api_err_unknown)
                            super.onError(e)
                        }
                        view.postDelayed({ view.openRegistrationDialog() }, 2000)
                    }
                }))
    }

    fun login(req: SignInReq) {
        compDisp.add(rootPresenter.login(req)
                .doOnSubscribe { view.closeLoginDialog() }
                .subscribeWith(object : ErrorSingleObserver<Unit>() {
                    override fun onSuccess(t: Unit) {
                        nextScreen()
                    }

                    override fun onError(e: Throwable) {
                        if (e is ApiError) {
                            when (e.statusCode) {
                                404 -> view.showError(R.string.message_api_err_auth_login)
                                403 -> view.showError(R.string.message_api_err_auth_password)
                                else -> view.showError(e.errorResId)
                            }
                        } else {
                            view.showError(R.string.message_api_err_unknown)
                            super.onError(e)
                        }
                        view.postDelayed({ view.openLoginDialog() }, 2000)
                    }
                }))
    }

    private fun nextScreen() {
        val nextScreen = when (rootPresenter.bottomHistory.currentItem) {
            BottomNavHistory.BottomItem.PROFILE -> ProfileScreen()
            BottomNavHistory.BottomItem.LOAD -> NewCardScreen()
            BottomNavHistory.BottomItem.MAIN -> MainScreen()
        }
        Flow.get(view).replaceTop(nextScreen, Direction.REPLACE)
    }
}
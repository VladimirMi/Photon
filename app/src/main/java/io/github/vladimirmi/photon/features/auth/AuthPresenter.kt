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
import io.github.vladimirmi.photon.flow.BottomNavHistory
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

/**
 * Created by Vladimir Mikhalev 25.06.2017.
 */

class AuthPresenter(model: IAuthModel, rootPresenter: RootPresenter)
    : BasePresenter<AuthView, IAuthModel>(model, rootPresenter) {

    override fun initToolbar() {
        rootPresenter.getNewToolbarBuilder().build()
    }

    override fun initView(view: AuthView) = Unit

    fun register(req: SignUpReq) {
        compDisp.add(rootPresenter.register(req)
                .doOnSubscribe {
                    view.closeRegistrationDialog()
                }
                .subscribe({}, {
                    // onError
                    if (it is ApiError) view.showError(it.errorResId)
                    else view.showError(R.string.message_api_err_unknown)
                    Observable.just(1).delay(2, TimeUnit.SECONDS, AndroidSchedulers.mainThread()).subscribe {
                        view.openRegistrationDialog()
                    }
                }, {
                    //onComplete
                    nextScreen()
                }))
    }

    fun login(req: SignInReq) {

        compDisp.add(rootPresenter.login(req)
                .doOnSubscribe {
                    view.closeLoginDialog()
                }
                .subscribe({}, {
                    // onError
                    if (it is ApiError) {
                        when (it.statusCode) {
                            404 -> view.showError(R.string.message_api_err_auth)
                            else -> view.showError(it.errorResId)
                        }
                    } else view.showError(R.string.message_api_err_unknown)
                    Observable.just(1).delay(2, TimeUnit.SECONDS, AndroidSchedulers.mainThread()).subscribe {
                        view.openLoginDialog()
                    }
                }, {
                    //onComplete
                    nextScreen()
                }))
    }

    private fun nextScreen() {
        val nextScreen = when (rootPresenter.bottomHistory!!.currentItem) {
            BottomNavHistory.BottomItem.PROFILE -> ProfileScreen()
            BottomNavHistory.BottomItem.LOAD -> NewCardScreen()
            BottomNavHistory.BottomItem.MAIN -> MainScreen()
        }
        Flow.get(view).replaceTop(nextScreen, Direction.REPLACE)
    }
}
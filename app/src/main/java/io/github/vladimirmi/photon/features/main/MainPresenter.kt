package io.github.vladimirmi.photon.features.main

import android.view.MenuItem
import flow.Flow
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.data.models.SignInReq
import io.github.vladimirmi.photon.data.models.SignUpReq
import io.github.vladimirmi.photon.data.network.ApiError
import io.github.vladimirmi.photon.features.root.MenuItemHolder
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.github.vladimirmi.photon.features.search.SearchScreen
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

/**
 * Developer Vladimir Mikhalev, 03.06.2017.
 */

class MainPresenter(model: IMainModel, rootPresenter: RootPresenter) :
        BasePresenter<MainView, IMainModel>(model, rootPresenter) {

    override fun initToolbar() {
        val loginActionProvider = LoginActionProvider(view.context,
                isLogin = rootPresenter.isUserAuth(),
                loginAction = view::openLoginDialog,
                registrationAction = view::openRegistrationDialog,
                logoutAction = this::logout
        )
        rootPresenter.getNewToolbarBuilder()
                .addAction(MenuItemHolder("Search", R.drawable.ic_action_search,
                        MenuItem.OnMenuItemClickListener {
                            Flow.get(view).set(SearchScreen())
                            return@OnMenuItemClickListener true
                        }))
                .addAction(MenuItemHolder("Login", R.drawable.ic_action_settings
                        , actionProvider = loginActionProvider
                ))
                .build()
    }

    override fun initView(view: MainView) {
        compDisp.add(subscribeOnPhotocards())
    }

    private fun subscribeOnPhotocards(): Disposable? {
        return model.getPhotoCards()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ view.setData(it) })
    }

    fun register(req: SignUpReq) {
        model.register(req)
                .doOnSubscribe { rootPresenter.showLoading() }
                .doAfterTerminate { rootPresenter.hideLoading() }
                .subscribe({}, {
                    // onError
                    if (it is ApiError) view.showMessage(it.errorResId)
                }, {
                    //onComplete
                    view.closeRegistrationDialog()
                    initToolbar()
                })
    }

    fun login(req: SignInReq) {
        model.login(req)
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
                    initToolbar()
                })
    }

    fun logout() {
        model.logout()
        initToolbar()
    }
}



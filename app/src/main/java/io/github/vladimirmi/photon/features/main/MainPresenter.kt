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

    val menuActions: (MenuItem) -> Unit = {
        when (it.itemId) {
            R.id.menu_signIn -> view.openLoginDialog()
            R.id.menu_signUp -> view.openRegistrationDialog()
            R.id.menu_logout -> logout()
        }
    }

    override fun initToolbar() {
        val popupMenu = if (rootPresenter.isUserAuth()) R.menu.submenu_main_screen_auth
        else R.menu.submenu_main_screen_not_auth

        rootPresenter.getNewToolbarBuilder()
                .addAction(MenuItemHolder("Search", R.drawable.ic_action_search,
                        actions = { Flow.get(view).set(SearchScreen()) }))
                .addAction(MenuItemHolder("Login",
                        iconResId = R.drawable.ic_action_settings,
                        popupMenu = popupMenu,
                        actions = menuActions))
                .build()
    }

    override fun initView(view: MainView) {
        compDisp.add(subscribeOnPhotocards())
    }

    private fun subscribeOnPhotocards(): Disposable {
        return model.getPhotoCards()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ view.setData(it) })
    }

    fun register(req: SignUpReq) {
        compDisp.add(rootPresenter.register(req)
                .subscribe({}, {
                    // onError
                    if (it is ApiError) view.showMessage(it.errorResId)
                }, {
                    //onComplete
                    view.closeRegistrationDialog()
                    initToolbar()
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
                    initToolbar()
                }))
    }

    fun logout() {
        rootPresenter.logout()
        initToolbar()
    }
}



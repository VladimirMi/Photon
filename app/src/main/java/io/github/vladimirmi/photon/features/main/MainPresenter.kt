package io.github.vladimirmi.photon.features.main

import android.view.MenuItem
import flow.Flow
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.data.models.SignInReq
import io.github.vladimirmi.photon.data.models.SignUpReq
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.data.network.ApiError
import io.github.vladimirmi.photon.features.photocard.PhotocardScreen
import io.github.vladimirmi.photon.features.root.MenuItemHolder
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.github.vladimirmi.photon.features.search.SearchScreen
import io.github.vladimirmi.photon.utils.ErrorObserver
import io.reactivex.disposables.Disposable
import timber.log.Timber

/**
 * Developer Vladimir Mikhalev, 03.06.2017.
 */

class MainPresenter(model: IMainModel, rootPresenter: RootPresenter) :
        BasePresenter<MainView, IMainModel>(model, rootPresenter) {

    private val menuActions: (MenuItem) -> Unit = {
        when (it.itemId) {
            R.id.menu_signIn -> view.openLoginDialog()
            R.id.menu_signUp -> view.openRegistrationDialog()
            R.id.menu_logout -> logout()
        }
    }

    private lateinit var cardsDisposable: Disposable

    override fun initToolbar() {
        val popupMenu = if (rootPresenter.isUserAuth()) R.menu.submenu_main_screen_auth
        else R.menu.submenu_main_screen_not_auth

        val searchIcon = if (model.isFiltered()) R.drawable.ic_action_search_active
        else R.drawable.ic_action_search

        rootPresenter.getNewToolbarBuilder()
                .addAction(MenuItemHolder("Search", searchIcon,
                        actions = { Flow.get(view).set(SearchScreen()) }))
                .addAction(MenuItemHolder("Login",
                        iconResId = R.drawable.ic_action_settings,
                        popupMenu = popupMenu,
                        actions = menuActions))
                .build()
    }

    override fun initView(view: MainView) {
        cardsDisposable = subscribeOnPhotocards()
        compDisp.add(cardsDisposable)
        if (model.isFiltered()) view.showFilterWarning()
    }

    private fun subscribeOnPhotocards(): Disposable {
        return model.getPhotoCards()
                .subscribeWith(object : ErrorObserver<List<Photocard>>() {
                    override fun onNext(it: List<Photocard>) {
                        view.setData(it)
                    }
                })
    }

    fun register(req: SignUpReq) {
        compDisp.add(rootPresenter.register(req)
                .doOnSubscribe { view.closeRegistrationDialog() }
                .subscribeWith(object : ErrorObserver<User>() {
                    override fun onComplete() {
                        initToolbar()
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
                .subscribeWith(object : ErrorObserver<User>() {
                    override fun onComplete() {
                        initToolbar()
                    }

                    override fun onError(e: Throwable) {
                        if (e is ApiError) {
                            when (e.statusCode) {
                                404 -> view.showError(R.string.message_api_err_auth)
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

    fun logout() {
        rootPresenter.logout()
        initToolbar()
    }

    fun resetFilter() {
        model.resetFilter()
        initToolbar()
        compDisp.remove(cardsDisposable)
        initView(view)
    }

    //todo network operation move to job
    fun showPhotoCard(photocard: Photocard) {
        model.addView(photocard).subscribeWith(ErrorObserver())
        Flow.get(view).set(PhotocardScreen(photocard.id, photocard.owner))
    }

    fun loadMore(page: Int, limit: Int) {
        Timber.e("loadMore: ")
        model.updatePhotocards(page * limit, limit)
    }
}



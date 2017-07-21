package io.github.vladimirmi.photon.features.main

import android.view.MenuItem
import flow.Flow
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.data.models.SignInReq
import io.github.vladimirmi.photon.data.models.SignUpReq
import io.github.vladimirmi.photon.data.models.dto.PhotocardDto
import io.github.vladimirmi.photon.data.network.ApiError
import io.github.vladimirmi.photon.features.photocard.PhotocardScreen
import io.github.vladimirmi.photon.features.root.MenuItemHolder
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.github.vladimirmi.photon.features.search.SearchScreen
import io.github.vladimirmi.photon.utils.AppConfig
import io.github.vladimirmi.photon.utils.ErrorObserver
import io.github.vladimirmi.photon.utils.afterNetCheck
import io.reactivex.disposables.Disposable
import timber.log.Timber

/**
 * Developer Vladimir Mikhalev, 03.06.2017.
 */

class MainPresenter(model: IMainModel, rootPresenter: RootPresenter) :
        BasePresenter<MainView, IMainModel>(model, rootPresenter) {

    private val menuActions: (MenuItem) -> Unit = {
        when (it.itemId) {
            R.id.menu_signIn -> view.afterNetCheck<MainView> { openLoginDialog() }
            R.id.menu_signUp -> view.afterNetCheck<MainView> { openRegistrationDialog() }
            R.id.menu_logout -> logout()
        }
    }

    private lateinit var cardsDisposable: Disposable
    private var updated = 0

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
        updated = Flow.getKey<MainScreen>(view)!!.updated
        if (updated == 0) {
            loadMore(0, AppConfig.PHOTOCARDS_PAGE_SIZE)
        } else {
            Flow.getKey<MainScreen>(view)!!.updated = 0
        }

        cardsDisposable = subscribeOnPhotocards()
        compDisp.add(cardsDisposable)
        if (model.isFiltered()) view.showFilterWarning()
    }

    private fun subscribeOnPhotocards(): Disposable {
        return model.getPhotoCards()
                .subscribeWith(object : ErrorObserver<List<PhotocardDto>>() {
                    override fun onNext(it: List<PhotocardDto>) {
                        val update = if (updated == 0) it.size else updated
                        view.setData(it, update)
                    }
                })
    }

    fun register(req: SignUpReq) {
        compDisp.add(rootPresenter.register(req)
                .doOnSubscribe { view.closeRegistrationDialog() }
                .subscribeWith(object : ErrorObserver<Unit>() {
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
                .subscribeWith(object : ErrorObserver<Unit>() {
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
        resubscribeCards()
    }

    fun showPhotoCard(photocard: PhotocardDto) {
        model.addView(photocard.id)
        Flow.get(view).set(PhotocardScreen(photocard.id, photocard.owner))
    }

    fun loadMore(page: Int, limit: Int) {
        Timber.e("loadMore: offset ${page * limit} limit $limit")
        model.updatePhotocards(page * limit, limit)
                .subscribeWith(object : ErrorObserver<Unit>(view) {
                    override fun onComplete() {
                        updated += limit
                        resubscribeCards()
                    }
                })
    }

    private fun resubscribeCards() {
        compDisp.remove(cardsDisposable)
        cardsDisposable = subscribeOnPhotocards()
        compDisp.add(cardsDisposable)
    }
}



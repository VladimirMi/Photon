package io.github.vladimirmi.photon.features.main

import android.view.MenuItem
import flow.Flow
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.data.models.dto.PhotocardDto
import io.github.vladimirmi.photon.data.models.req.SignInReq
import io.github.vladimirmi.photon.data.models.req.SignUpReq
import io.github.vladimirmi.photon.data.network.ApiError
import io.github.vladimirmi.photon.features.photocard.PhotocardScreen
import io.github.vladimirmi.photon.features.root.MenuItemHolder
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.github.vladimirmi.photon.features.search.SearchScreen
import io.github.vladimirmi.photon.utils.AppConfig
import io.github.vladimirmi.photon.utils.ErrorCompletableObserver
import io.github.vladimirmi.photon.utils.ErrorObserver
import io.reactivex.disposables.Disposable

/**
 * Developer Vladimir Mikhalev, 03.06.2017.
 */

class MainPresenter(model: IMainModel, rootPresenter: RootPresenter) :
        BasePresenter<MainView, IMainModel>(model, rootPresenter) {

    private val menuActions: (MenuItem) -> Unit = {
        when (it.itemId) {
            R.id.menu_signIn -> rootPresenter.afterNetCheck(view) { openLoginDialog() }
            R.id.menu_signUp -> rootPresenter.afterNetCheck(view) { openRegistrationDialog() }
            R.id.menu_logout -> logout()
        }
    }

    private lateinit var cardsDisposable: Disposable
    private var updated = 0
    private lateinit var mainScreen: MainScreen

    override fun initToolbar() {
        val popupMenu = if (rootPresenter.isUserAuth()) R.menu.submenu_main_screen_auth
        else R.menu.submenu_main_screen_not_auth

        val searchIcon = if (model.isFiltered()) R.drawable.ic_action_search_active
        else R.drawable.ic_action_search

        rootPresenter.getNewToolbarBuilder()
                .addAction(MenuItemHolder("Search", searchIcon,
                        actions = { openSearchScreen() }))
                .addAction(MenuItemHolder("Login",
                        iconResId = R.drawable.ic_action_settings,
                        popupMenu = popupMenu,
                        actions = menuActions))
                .build()

    }

    override fun initView(view: MainView) {
        mainScreen = Flow.getKey<MainScreen>(view)!!
        loadCardsIfNeeded()
        updateModel()
        cardsDisposable = subscribeOnPhotocards()
        compDisp.add(cardsDisposable)
        if (model.isFiltered()) view.showFilterWarning()
    }

    private fun loadCardsIfNeeded() {
        updated = mainScreen.updated
        if (updated == 0) {
            loadMore(0, AppConfig.PHOTOCARDS_PAGE_SIZE)
        } else {
            mainScreen.updated = 0
        }
    }

    private fun updateModel() {
        if (model.isFiltered()) return
        model.tagsQuery = mainScreen.tagsQuery
        model.filtersQuery = mainScreen.filtersQuery
        model.queryPage = mainScreen.queryPage
        model.makeQuery()
        initToolbar()
    }

    private fun subscribeOnPhotocards(): Disposable {
        return model.getPhotoCards()
                .subscribeWith(object : ErrorObserver<List<PhotocardDto>>() {
                    override fun onNext(it: List<PhotocardDto>) {
                        val update = if (updated == 0) it.size else updated
                        view?.setData(it, update)
                    }
                })
    }

    override fun onExitScope() {
        mainScreen.tagsQuery = model.tagsQuery
        mainScreen.filtersQuery = model.filtersQuery
        mainScreen.queryPage = model.queryPage
        super.onExitScope()
    }

    fun register(req: SignUpReq) {
        compDisp.add(rootPresenter.register(req)
                .doOnSubscribe { view.closeRegistrationDialog() }
                .subscribeWith(object : ErrorCompletableObserver() {
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
                .subscribeWith(object : ErrorCompletableObserver() {
                    override fun onComplete() {
                        initToolbar()
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

    private fun logout() {
        rootPresenter.logout()
        initToolbar()
    }

    fun resetFilter() {
        model.resetFilter()
        initToolbar()
        resubscribeCards()
    }

    fun openPhotocardScreen(photocard: PhotocardDto) {
        compDisp.add(model.addView(photocard.id)
                .doOnNext { Flow.get(view).set(PhotocardScreen(photocard.id, photocard.owner)) }
                .subscribeWith(ErrorObserver()))
    }

    private fun openSearchScreen() {
        Flow.get(view).set(SearchScreen())
    }

    fun loadMore(page: Int, limit: Int) {
        compDisp.add(model.updatePhotocards(page * limit, limit)
                .doOnComplete {
                    updated += limit
                    resubscribeCards()
                }
                .subscribeWith(ErrorCompletableObserver())
        )
    }

    private fun resubscribeCards() {
        compDisp.remove(cardsDisposable)
        cardsDisposable = subscribeOnPhotocards()
        compDisp.add(cardsDisposable)
    }
}



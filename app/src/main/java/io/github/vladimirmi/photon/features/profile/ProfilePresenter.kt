package io.github.vladimirmi.photon.features.profile

import android.view.MenuItem
import flow.Flow
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.data.models.Album
import io.github.vladimirmi.photon.data.models.NewAlbumReq
import io.github.vladimirmi.photon.data.models.SignInReq
import io.github.vladimirmi.photon.data.models.SignUpReq
import io.github.vladimirmi.photon.data.network.ApiError
import io.github.vladimirmi.photon.features.album.AlbumScreen
import io.github.vladimirmi.photon.features.root.MenuItemHolder
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.reactivex.disposables.Disposable

class ProfilePresenter(model: IProfileModel, rootPresenter: RootPresenter)
    : BasePresenter<ProfileView, IProfileModel>(model, rootPresenter) {

    val menuActions: (MenuItem) -> Unit = {
        when (it.itemId) {
            R.id.menu_new_album -> view.openNewAlbumDialog()
            R.id.menu_edit_profile -> editProfile()
            R.id.menu_change_avatar -> changeAvatar()
            R.id.menu_logout -> logout()
            R.id.menu_signIn -> view.openLoginDialog()
            R.id.menu_signUp -> view.openRegistrationDialog()
        }
    }

    override fun initToolbar() {
        val popupMenu = if (model.isUserAuth()) R.menu.submenu_profile_screen else
            R.menu.submenu_main_screen_not_auth
        rootPresenter.getNewToolbarBuilder()
                .addAction(MenuItemHolder("Actions",
                        iconResId = R.drawable.ic_action_more,
                        actions = menuActions,
                        popupMenu = popupMenu))
                .build()
    }

    override fun initView(view: ProfileView) {
        view.closeLoginDialog()
        view.closeRegistrationDialog()
        if (!model.isUserAuth()) {
            view.showAuth()
        } else {
            compDisp.add(subscribeOnProfile())
            view.showProfile()
        }
    }

    private fun subscribeOnProfile(): Disposable {
        return model.getProfile()
                .subscribe { view.setProfile(it) }
    }

    fun register(req: SignUpReq) {
        compDisp.add(rootPresenter.register(req)
                .subscribe({}, {
                    // onError
                    if (it is ApiError) view.showMessage(it.errorResId)
                }, {
                    //onComplete
                    initView(view)
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
                    initView(view)
                }))
    }

    fun showAlbum(album: Album) = Flow.get(view).set(AlbumScreen(album))

    private fun logout() {
        rootPresenter.logout()
        initToolbar()
        initView(view)
    }

    private fun changeAvatar() {
        //todo implement
    }

    private fun editProfile() {
        //todo implement
    }

    fun createNewAlbum(newAlbumReq: NewAlbumReq) {
        compDisp.add(model.createAlbum(newAlbumReq).subscribe())
    }
}


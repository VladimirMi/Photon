package io.github.vladimirmi.photon.features.profile

import android.view.MenuItem
import flow.Direction
import flow.Flow
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.data.models.NewAlbumReq
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.features.album.AlbumScreen
import io.github.vladimirmi.photon.features.auth.AuthScreen
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
        }
    }

    override fun initToolbar() {
        rootPresenter.getNewToolbarBuilder()
                .addAction(MenuItemHolder("Actions",
                        iconResId = R.drawable.ic_action_more,
                        actions = menuActions,
                        popupMenu = R.menu.submenu_profile_screen))
                .build()
    }

    override fun initView(view: ProfileView) {
        compDisp.add(subscribeOnProfile())
    }

    private fun subscribeOnProfile(): Disposable {
        return model.getProfile()
                .subscribe { view.setProfile(it) }
    }

    fun showAlbum(album: Album) = Flow.get(view).set(AlbumScreen(album))

    private fun logout() {
        rootPresenter.logout()
        Flow.get(view).replaceTop(AuthScreen(), Direction.REPLACE)
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


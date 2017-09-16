package io.github.vladimirmi.photon.presentation.profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.view.MenuItem
import flow.Direction
import flow.Flow
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.req.ProfileEditReq
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.domain.models.AlbumDto
import io.github.vladimirmi.photon.domain.models.UserDto
import io.github.vladimirmi.photon.presentation.album.AlbumScreen
import io.github.vladimirmi.photon.presentation.auth.AuthScreen
import io.github.vladimirmi.photon.presentation.root.MenuItemHolder
import io.github.vladimirmi.photon.presentation.root.RootPresenter
import io.github.vladimirmi.photon.utils.Constants
import io.github.vladimirmi.photon.utils.ErrorObserver
import io.reactivex.disposables.Disposable
import javax.inject.Inject

@DaggerScope(ProfileScreen::class)
class ProfilePresenter
@Inject constructor(model: ProfileInteractor, rootPresenter: RootPresenter)
    : BasePresenter<ProfileView, ProfileInteractor>(model, rootPresenter) {

    private lateinit var profile: UserDto

    private val menuActions: (MenuItem) -> Unit = {
        when (it.itemId) {
            R.id.menu_new_album -> view.openNewAlbumDialog()
            R.id.menu_edit_profile -> view.openEditProfileDialog()
            R.id.menu_change_avatar -> changeAvatar()
            R.id.menu_logout -> logout()
        }
    }

    override fun initToolbar() {
        rootPresenter.getNewToolbarBuilder()
                .setToolbarTitleId(R.string.profile_title)
                .addAction(MenuItemHolder("Actions",
                        iconResId = R.drawable.ic_action_more,
                        actions = menuActions,
                        popupMenu = R.menu.submenu_profile_screen))
                .build()
    }

    override fun initView(view: ProfileView) {
        compDisp.add(subscribeOnProfile())
        compDisp.add(subscribeOnAlbums())
    }

    private fun subscribeOnProfile(): Disposable {
        return model.getProfile()
                .subscribe {
                    profile = it
                    view.setProfile(it)
                }
    }

    private fun subscribeOnAlbums(): Disposable {
        return model.getAlbums()
                .subscribe { view.setAlbums(it) }
    }

    fun showAlbum(album: AlbumDto) {
        Flow.get(view).set(AlbumScreen(album.id))
    }

    private fun logout() {
        rootPresenter.logout()
        Flow.get(view).replaceTop(AuthScreen(), Direction.REPLACE)
    }

    private fun changeAvatar() {
        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (rootPresenter.checkAndRequestPermissions(permissions, Constants.REQUEST_GALLERY)) {
            takePhoto()
        }
    }

    fun createNewAlbum(album: Album) {
        view.closeNewAlbumDialog()
        compDisp.add(model.createAlbum(album)
                .subscribeWith(ErrorObserver(view)))
    }

    fun editProfile(request: ProfileEditReq) {
        view?.closeEditProfileDialog()
        if (!profileChanged(request)) return

        compDisp.add(model.editProfile(request)
                .subscribeWith(ErrorObserver(view)))
    }

    private fun profileChanged(request: ProfileEditReq): Boolean {
        return request.name != profile.name ||
                request.login != profile.login ||
                !request.avatar.startsWith("http")
    }

    private fun takePhoto() {
        val intent = Intent()
        intent.type = Constants.MIME_TYPE_IMAGE
        intent.action = Intent.ACTION_OPEN_DOCUMENT
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        rootPresenter.startActivityForResult(intent, Constants.REQUEST_GALLERY)
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        val requestCanceled = grantResults.contains(PackageManager.PERMISSION_DENIED) || grantResults.isEmpty()
        if (requestCanceled) {
            rootPresenter.showPermissionSnackBar()
        } else if (requestCode == Constants.REQUEST_GALLERY) {
            takePhoto()
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.REQUEST_GALLERY && data != null && data.data != null) {
                editAvatar(data.data.toString())
            }
        }
    }

    private fun editAvatar(uri: String) {
        val request = ProfileEditReq(
                name = profile.name,
                login = profile.login,
                avatar = uri)
        editProfile(request)
    }
}


package io.github.vladimirmi.photon.features.profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.view.MenuItem
import flow.Direction
import flow.Flow
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.dto.UserDto
import io.github.vladimirmi.photon.data.models.req.EditProfileReq
import io.github.vladimirmi.photon.data.models.req.NewAlbumReq
import io.github.vladimirmi.photon.data.network.ApiError
import io.github.vladimirmi.photon.features.album.AlbumScreen
import io.github.vladimirmi.photon.features.auth.AuthScreen
import io.github.vladimirmi.photon.features.root.MenuItemHolder
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.github.vladimirmi.photon.utils.Constants
import io.github.vladimirmi.photon.utils.ErrorObserver
import io.github.vladimirmi.photon.utils.JobStatus
import io.reactivex.disposables.Disposable

class ProfilePresenter(model: IProfileModel, rootPresenter: RootPresenter)
    : BasePresenter<ProfileView, IProfileModel>(model, rootPresenter) {

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

    fun createNewAlbum(newAlbumReq: NewAlbumReq) {
        compDisp.add(model.createAlbum(newAlbumReq)
                .doOnSubscribe { view.closeNewAlbumDialog() }
                .subscribeWith(object : ErrorObserver<JobStatus>() {
                    override fun onComplete() {
                        view.showMessage(R.string.album_create_success)
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        if (e is ApiError) view.showError(e.errorResId)
                    }
                }))
    }

    fun editProfile(profileReq: EditProfileReq) {
        view.closeEditProfileDialog()
        if (!profileReq.avatarChanged) profileReq.avatar = profile.avatar
        if (!profileChanged(profileReq)) return

        compDisp.add(model.editProfile(profileReq)
                .subscribeWith(object : ErrorObserver<JobStatus>() {
                    override fun onComplete() {
                        view.showMessage(R.string.profile_edit_success)
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        if (e is ApiError) view.showError(e.errorResId)
                    }
                }))
    }

    private fun profileChanged(profileReq: EditProfileReq): Boolean {
        return profile.name != profileReq.name ||
                profile.login != profileReq.login ||
                profileReq.avatarChanged
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
        val profileReq = EditProfileReq(profile.name, profile.login, uri)
        profileReq.avatarChanged = true
        editProfile(profileReq)
    }
}


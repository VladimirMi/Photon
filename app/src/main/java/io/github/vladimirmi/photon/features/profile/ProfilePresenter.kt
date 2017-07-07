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
import io.github.vladimirmi.photon.data.models.NewAlbumReq
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.features.album.AlbumScreen
import io.github.vladimirmi.photon.features.auth.AuthScreen
import io.github.vladimirmi.photon.features.root.MenuItemHolder
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.github.vladimirmi.photon.utils.Constants
import io.github.vladimirmi.photon.utils.ErrorObserver
import io.reactivex.disposables.Disposable

class ProfilePresenter(model: IProfileModel, rootPresenter: RootPresenter)
    : BasePresenter<ProfileView, IProfileModel>(model, rootPresenter) {

    private var editMode = false
    private lateinit var profile: User

    private val menuActions: (MenuItem) -> Unit = {
        when (it.itemId) {
            R.id.menu_new_album -> view.openNewAlbumDialog()
            R.id.menu_edit_profile -> setEditable(true)
            R.id.menu_change_avatar -> changeAvatar()
            R.id.menu_logout -> logout()
        }
    }
    private val submitAction: (MenuItem) -> Unit = { submit() }

    override fun initToolbar() {
        val builder = rootPresenter.getNewToolbarBuilder()
                .setToolbarTitleId(R.string.profile_title)

        if (editMode) builder.addAction(MenuItemHolder("Submit",
                R.drawable.ic_action_submit, submitAction))

        builder.addAction(MenuItemHolder("Actions",
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
                .subscribe {
                    profile = it
                    view.setProfile(it)
                }
    }

    fun showAlbum(album: Album) = Flow.get(view).set(AlbumScreen(album))

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
                .subscribeWith(object : ErrorObserver<Unit>() {
                    override fun onComplete() {
                        view.closeNewAlbumDialog()
                    }
                }))
    }

    private fun setEditable(boolean: Boolean) {
        editMode = boolean
        view.setEditable(editMode)
        initToolbar()
    }

    private fun submit() {
        setEditable(false)
        val name = view.name.text.toString()
        val login = view.login.text.toString()
        var infoChanged = false
        if (profile.name != name || profile.login != login) {
            infoChanged = true
            profile.name = name
            profile.login = login
        }
        if (infoChanged) {
            compDisp.add(model.editProfile(profile).subscribe())
        }
    }

    //todo move to root presenter
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
                model.saveAvatar(data.data.toString(), profile)
            }
        }
    }
}


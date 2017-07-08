package io.github.vladimirmi.photon.features.profile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.support.v7.widget.GridLayoutManager
import android.util.AttributeSet
import android.view.inputmethod.InputMethodManager
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.data.models.NewAlbumReq
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.features.main.AlbumAdapter
import io.github.vladimirmi.photon.flow.FlowLifecycles
import io.github.vladimirmi.photon.ui.NewAlbumDialog
import io.github.vladimirmi.photon.ui.setRoundAvatarWithBorder
import kotlinx.android.synthetic.main.view_profile.view.*

/**
 * Created by Vladimir Mikhalev 15.06.2017.
 */

class ProfileView(context: Context, attrs: AttributeSet)
    : BaseView<ProfilePresenter, ProfileView>(context, attrs),
        FlowLifecycles.ActivityResultListener, FlowLifecycles.PermissionRequestListener {

    private val albumAction: (Album) -> Unit = { showAlbum(it) }
    private val adapter = AlbumAdapter(albumAction)

    private val newAlbumAction: (NewAlbumReq) -> Unit = { presenter.createNewAlbum(it) }
    private val newAlbumDialog = NewAlbumDialog(this, newAlbumAction)

    val login by lazy { user_login }
    val name by lazy { user_name }

    override fun initDagger(context: Context) {
        DaggerService.getComponent<ProfileScreen.Component>(context).inject(this)
    }

    override fun initView() {
        @Suppress("UsePropertyAccessSyntax")
        album_list.setLayoutManager(GridLayoutManager(context, 2))
        album_list.adapter = adapter
    }

    private var curAvatarPath = ""
    val namePrefix = "/  "

    @SuppressLint("SetTextI18n")
    fun setProfile(user: User) {
        login.setText(user.login)
        name.setText(namePrefix + user.name)
        if (user.avatar != curAvatarPath) {
            setRoundAvatarWithBorder(user.avatar, user_avatar, 0f)
            curAvatarPath = user.avatar
        }
        val albums = user.albums.filter { it.active }
        album_count.text = albums.size.toString()
        card_count.text = albums.fold(0, { acc, album ->
            acc + album.photocards.count { it.active }
        }).toString()
        adapter.updateData(albums)
    }

    private fun showAlbum(album: Album) = presenter.showAlbum(album)

    fun openNewAlbumDialog() = newAlbumDialog.show()

    fun closeNewAlbumDialog() = newAlbumDialog.hide()


    fun setEditable(editMode: Boolean) {
        login.isEnabled = editMode
        if (editMode) {
            login.requestFocus()
            login.setSelection(login.length())
            name.setSelection(name.length())
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(name, InputMethodManager.SHOW_IMPLICIT)
        }
        name.isEnabled = editMode
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        presenter.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        presenter.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}


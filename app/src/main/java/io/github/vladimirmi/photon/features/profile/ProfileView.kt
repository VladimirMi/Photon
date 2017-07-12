package io.github.vladimirmi.photon.features.profile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.support.v7.widget.GridLayoutManager
import android.util.AttributeSet
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.data.models.EditProfileReq
import io.github.vladimirmi.photon.data.models.NewAlbumReq
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.features.main.AlbumAdapter
import io.github.vladimirmi.photon.flow.FlowLifecycles
import io.github.vladimirmi.photon.ui.EditProfileDialog
import io.github.vladimirmi.photon.ui.NewAlbumDialog
import io.github.vladimirmi.photon.utils.setRoundAvatarWithBorder
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

    private val loginView by lazy { user_login }
    private val nameView by lazy { user_name }
    private val albumsView by lazy { album_list }
    private val albumCountView by lazy { album_count }
    private val cardCountView by lazy { card_count }
    private val avatarView by lazy { user_avatar }

    private val editProfileAction: (EditProfileReq) -> Unit = { presenter.editProfile(it) }
    private val editProfileDialog = EditProfileDialog(this, editProfileAction)

    override fun initDagger(context: Context) {
        DaggerService.getComponent<ProfileScreen.Component>(context).inject(this)
    }

    override fun initView() {
        @Suppress("UsePropertyAccessSyntax")
        albumsView.setLayoutManager(GridLayoutManager(context, 2))
        albumsView.adapter = adapter
    }

    private var curAvatarPath = ""
    val namePrefix = "/  "

    @SuppressLint("SetTextI18n")
    fun setProfile(user: User) {
        editProfileDialog.initFields(user.login, user.name)
        loginView.text = user.login
        nameView.text = namePrefix + user.name
        if (user.avatar != curAvatarPath) {
            avatarView.setRoundAvatarWithBorder(user.avatar, 0f)
            curAvatarPath = user.avatar
        }
    }


    fun setAlbums(list: List<Album>) {
        val albums = list.filter { it.active }
        albumCountView.text = albums.count { !it.isFavorite }.toString()
        cardCountView.text = albums.filter { !it.isFavorite }
                .fold(0, { acc, album -> acc + album.photocards.count { it.active } })
                .toString()
        adapter.updateData(albums)
    }

    private fun showAlbum(album: Album) = presenter.showAlbum(album)

    fun openNewAlbumDialog() = newAlbumDialog.show()
    fun closeNewAlbumDialog() = newAlbumDialog.hide()
    fun openEditProfileDialog() = editProfileDialog.show()
    fun closeEditProfileDialog() = editProfileDialog.hide()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        presenter.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        presenter.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onViewRestored() {
        super.onViewRestored()
        newAlbumDialog.subscribe()
        editProfileDialog.subscribe()
    }

    override fun onViewDestroyed(removedByFlow: Boolean) {
        super.onViewDestroyed(removedByFlow)
        newAlbumDialog.unsubscribe()
        editProfileDialog.unsubscribe()
    }
}


package io.github.vladimirmi.photon.features.profile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.support.v7.widget.GridLayoutManager
import android.util.AttributeSet
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.dto.UserDto
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
    private lateinit var profile: UserDto

    private val albumAction: (AlbumDto) -> Unit = { showAlbum(it) }
    private val adapter = AlbumAdapter(albumAction)

    private val newAlbumAction: (AlbumDto) -> Unit = { presenter.createNewAlbum(it) }
    private val newAlbumDialog = NewAlbumDialog(this, newAlbumAction)

    private val loginView by lazy { user_login }
    private val nameView by lazy { user_name }
    private val albumsView by lazy { album_list }
    private val albumCountView by lazy { album_count }
    private val cardCountView by lazy { card_count }
    private val avatarView by lazy { user_avatar }

    private val editProfileAction: (UserDto) -> Unit = { presenter.editProfile(it) }
    private val editProfileDialog by lazy { EditProfileDialog(this, editProfileAction, profile) }

    override fun initDagger(context: Context) {
        DaggerService.getComponent<ProfileScreen.Component>(context).inject(this)
    }

    override fun initView() {
        @Suppress("UsePropertyAccessSyntax")
        albumsView.setLayoutManager(GridLayoutManager(context, 2))
        albumsView.adapter = adapter
    }

    private var curAvatarPath = ""
    private val namePrefix = "/  "

    @SuppressLint("SetTextI18n")
    fun setProfile(user: UserDto) {
        profile = user
        loginView.text = user.login
        nameView.text = namePrefix + user.name
        if (user.avatar != curAvatarPath) {
            avatarView.setRoundAvatarWithBorder(user.avatar)
            curAvatarPath = user.avatar
        }
    }


    fun setAlbums(albums: List<AlbumDto>) {
        albumCountView.text = albums.size.toString()
        cardCountView.text = albums.filter { !it.isFavorite }
                .fold(0, { acc, album -> acc + album.photocards.size })
                .toString()
        adapter.updateData(albums)
    }

    private fun showAlbum(album: AlbumDto) = presenter.showAlbum(album)

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


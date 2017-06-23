package io.github.vladimirmi.photon.features.profile

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.util.AttributeSet
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.data.models.*
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.features.main.AlbumAdapter
import io.github.vladimirmi.photon.ui.LoginDialog
import io.github.vladimirmi.photon.ui.NewAlbumDialog
import io.github.vladimirmi.photon.ui.RegistrationDialog
import io.github.vladimirmi.photon.ui.setRoundAvatarWithBorder
import kotlinx.android.synthetic.main.view_profile.view.*
import kotlinx.android.synthetic.main.view_profile_not_auth.view.*
import timber.log.Timber

/**
 * Created by Vladimir Mikhalev 15.06.2017.
 */

class ProfileView(context: Context, attrs: AttributeSet)
    : BaseView<ProfilePresenter, ProfileView>(context, attrs) {

    val albumAction: (Album) -> Unit = { showAlbum(it) }
    val adapter = AlbumAdapter(albumAction)

    val registrationAction: (SignUpReq) -> Unit = { presenter.register(it) }
    val loginAction: (SignInReq) -> Unit = { presenter.login(it) }
    val newAlbumAction: (NewAlbumReq) -> Unit = { presenter.createNewAlbum(it) }

    val registrationDialog = RegistrationDialog(this, registrationAction)
    val loginDialog = LoginDialog(this, loginAction)
    val newAlbumDialog = NewAlbumDialog(this, newAlbumAction)

    override fun initDagger(context: Context) {
        DaggerService.getComponent<ProfileScreen.Component>(context).inject(this)
    }

    override fun initView() {
        @Suppress("UsePropertyAccessSyntax")
        album_list.setLayoutManager(GridLayoutManager(context, 2))
        album_list.adapter = adapter
        login_btn.setOnClickListener { openLoginDialog() }
        registration_btn.setOnClickListener { openRegistrationDialog() }
    }

    fun showAuth() {
        auth_view.visibility = VISIBLE
        profile_view.visibility = GONE
    }

    fun showProfile() {
        auth_view.visibility = GONE
        profile_view.visibility = VISIBLE
    }

    private var curAvatarPath = ""
    @SuppressLint("SetTextI18n")
    fun setProfile(user: User) {
        Timber.e(user.name)
        user_login.text = user.login
        user_name.text = "/  " + user.name
        album_count.text = user.albumCount.toString()
        card_count.text = user.photocardCount.toString()
        if (user.avatar != curAvatarPath) {
            setRoundAvatarWithBorder(user.avatar, user_avatar, 0f)
            curAvatarPath = user.avatar
        }
        adapter.updateData(user.albums)
    }

    private fun showAlbum(album: Album) = presenter.showAlbum(album)

    fun openRegistrationDialog() = registrationDialog.dialog.show()

    fun openLoginDialog() = loginDialog.dialog.show()

    fun closeRegistrationDialog() = registrationDialog.dialog.cancel()

    fun closeLoginDialog() = loginDialog.dialog.cancel()

    fun openNewAlbumDialog() = newAlbumDialog.dialog.show()

    fun closeNewAlbumDialog() = newAlbumDialog.dialog.cancel()
}


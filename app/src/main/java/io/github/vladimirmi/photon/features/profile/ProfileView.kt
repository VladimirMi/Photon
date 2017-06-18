package io.github.vladimirmi.photon.features.profile

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.util.AttributeSet
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.data.models.Album
import io.github.vladimirmi.photon.data.models.User
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.features.main.AlbumAdapter
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

    override fun onBackPressed() = false

    override fun initDagger(context: Context) {
        DaggerService.getComponent<ProfileScreen.Component>(context).inject(this)
    }

    override fun initView() {
        album_list.layoutManager = GridLayoutManager(context, 2)
        album_list.adapter = adapter
    }

    fun showAuth() {
        auth_view.visibility = VISIBLE
        profile_view.visibility = GONE
    }

    fun showProfile() {
        auth_view.visibility = GONE
        profile_view.visibility = VISIBLE
    }

    fun setProfile(user: User) {
        Timber.e(user.name)
        profile_login.text = user.login
        profile_name.text = user.name
        album_num.text = user.albumCount.toString()
        card_num.text = user.photocardCount.toString()
        setRoundAvatarWithBorder(user.avatar, profile_avatar, 0f)
        adapter.updateData(user.albums)
    }

    private fun showAlbum(album: Album) {
        //TODO("not implemented")
    }
}


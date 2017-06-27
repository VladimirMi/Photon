package io.github.vladimirmi.photon.features.author

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.util.AttributeSet
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.features.main.AlbumAdapter
import io.github.vladimirmi.photon.ui.setRoundAvatarWithBorder
import kotlinx.android.synthetic.main.view_profile.view.*
import timber.log.Timber

/**
 * Created by Vladimir Mikhalev 15.06.2017.
 */

class AuthorView(context: Context, attrs: AttributeSet)
    : BaseView<AuthorPresenter, AuthorView>(context, attrs) {

    val albumAction: (Album) -> Unit = { showAlbum(it) }
    val adapter = AlbumAdapter(albumAction)

    override fun initDagger(context: Context) {
        DaggerService.getComponent<AuthorScreen.Component>(context).inject(this)
    }

    override fun initView() {
        album_list.layoutManager = GridLayoutManager(context, 2)
        adapter.authorMode = true
        album_list.adapter = adapter
    }

    private var curAvatarPath = ""
    @SuppressLint("SetTextI18n")
    fun setUser(user: User) {
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
}


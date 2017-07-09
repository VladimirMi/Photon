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

/**
 * Created by Vladimir Mikhalev 15.06.2017.
 */

class AuthorView(context: Context, attrs: AttributeSet)
    : BaseView<AuthorPresenter, AuthorView>(context, attrs) {

    private val albumAction: (Album) -> Unit = { showAlbum(it) }
    private val adapter = AlbumAdapter(albumAction)

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
        user_login.setText(user.login)
        user_name.setText("/  ${user.name}")
        if (user.avatar != curAvatarPath) {
            setRoundAvatarWithBorder(user.avatar, user_avatar, 0f)
            curAvatarPath = user.avatar
        }

        val albums = user.albums.filter { it.active }
        album_count.text = albums.count { !it.isFavorite }.toString()
        card_count.text = albums.filter { !it.isFavorite }
                .fold(0, { acc, album -> acc + album.photocards.count { it.active } })
                .toString()
        adapter.updateData(albums)
    }

    private fun showAlbum(album: Album) = presenter.showAlbum(album)
}


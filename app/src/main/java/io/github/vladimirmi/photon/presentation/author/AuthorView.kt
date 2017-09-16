package io.github.vladimirmi.photon.presentation.author

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.util.AttributeSet
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.domain.models.AlbumDto
import io.github.vladimirmi.photon.domain.models.UserDto
import io.github.vladimirmi.photon.presentation.main.AlbumAdapter
import io.github.vladimirmi.photon.utils.setRoundAvatarWithBorder
import kotlinx.android.synthetic.main.view_profile.view.*

/**
 * Created by Vladimir Mikhalev 15.06.2017.
 */

class AuthorView(context: Context, attrs: AttributeSet)
    : BaseView<AuthorPresenter, AuthorView>(context, attrs) {

    private val albumAction: (AlbumDto) -> Unit = { showAlbum(it) }
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
    fun setUser(user: UserDto) {
        user_login.text = user.login
        user_name.text = "/  ${user.name}"
        if (user.avatar != curAvatarPath) {
            user_avatar.setRoundAvatarWithBorder(user.avatar)
            curAvatarPath = user.avatar
        }
    }

    fun setAlbums(albums: List<AlbumDto>) {
        album_count.text = albums.size.toString()
        card_count.text = albums.filter { !it.isFavorite }
                .fold(0, { acc, album -> acc + album.photocards.size })
                .toString()
        adapter.updateData(albums)
    }


    private fun showAlbum(album: AlbumDto) = presenter.showAlbum(album)
}


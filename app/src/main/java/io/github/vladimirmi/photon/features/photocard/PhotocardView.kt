package io.github.vladimirmi.photon.features.photocard

import android.content.Context
import android.content.Intent
import android.support.design.widget.Snackbar
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.flow.FlowLifecycles
import io.github.vladimirmi.photon.ui.TagView
import io.github.vladimirmi.photon.utils.setImage
import io.github.vladimirmi.photon.utils.setRoundAvatarWithBorder
import kotlinx.android.synthetic.main.screen_photocard.view.*

/**
 * Created by Vladimir Mikhalev 14.06.2017.
 */

class PhotocardView(context: Context, attrs: AttributeSet)
    : BaseView<PhotocardPresenter, PhotocardView>(context, attrs),
        FlowLifecycles.PermissionRequestListener, FlowLifecycles.ActivityResultListener {

    override fun initDagger(context: Context) {
        DaggerService.getComponent<PhotocardScreen.Component>(context).inject(this)
    }

    override fun initView() {
        user_avatar.setOnClickListener { presenter.showAuthor() }
        pull_to_zoom.subscribe()
    }

    override fun onViewDestroyed(removedByFlow: Boolean) {
        super.onViewDestroyed(removedByFlow)
        pull_to_zoom.unsubscribe()
    }

    private var curAvatarPath = ""
    fun setUser(user: User) {
        if (user.avatar != curAvatarPath) {
            user_avatar.setRoundAvatarWithBorder(user.avatar, 0f)
            curAvatarPath = user.avatar
        }
        user_name.text = user.name
        val albums = user.albums.filter { it.active }
        album_count.text = albums.count { !it.isFavorite }.toString()
        card_count.text = albums.filter { !it.isFavorite }
                .fold(0, { acc, album -> acc + album.photocards.count { it.active } })
                .toString()
    }

    private var curImagePath = ""
    fun setPhotocard(photocard: Photocard) {
        if (curImagePath != photocard.photo) {
            photo.setImage(photocard.photo)
            curImagePath = photocard.photo
        }
        card_name.text = photocard.title
        val flexbox = LayoutInflater.from(context).inflate(R.layout.view_search_tags, this, false)
        flexbox as ViewGroup
        photocard.tags.forEach {
            val tag = TagView(context, it.value, null)
            flexbox.addView(tag)
        }
        photocard_tags_wrapper.addView(flexbox)
    }

    fun setFavorite(favorite: Boolean) {
        ic_favorite.visibility = if (favorite) View.VISIBLE else View.GONE
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        presenter.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        presenter.onActivityResult(requestCode, resultCode, data)
    }

    fun showLoadSnackbar(callback: () -> Unit) {
        Snackbar.make(this, R.string.photocard_message_download, Snackbar.LENGTH_LONG)
                .setAction(R.string.photocard_message_download_open) { callback() }
                .show()
    }
}


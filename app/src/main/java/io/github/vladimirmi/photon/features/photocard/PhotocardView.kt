package io.github.vladimirmi.photon.features.photocard

import android.content.Context
import android.content.Intent
import android.support.design.widget.Snackbar
import android.util.AttributeSet
import android.view.View
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.data.models.dto.PhotocardDto
import io.github.vladimirmi.photon.data.models.dto.UserDto
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

    private val userAvatarIm by lazy { user_avatar }
    private val pullToZoomWrapper by lazy { pull_to_zoom }
    private val flexbox by lazy { flex_box }
    private val userNameTx by lazy { user_name }
    private val albumCountTx by lazy { album_count }
    private val cardCountTx by lazy { card_count }
    private val cardNameTx by lazy { card_name }
    private val favoriteIcon by lazy { ic_favorite }

    override fun initView() {
        userAvatarIm.setOnClickListener { presenter.showAuthor() }
        pullToZoomWrapper.subscribe()
    }

    override fun onViewDestroyed(removedByFlow: Boolean) {
        super.onViewDestroyed(removedByFlow)
        pullToZoomWrapper.unsubscribe()
    }

    private var curAvatarPath = ""

    fun setUser(user: UserDto) {
        if (user.avatar != curAvatarPath) {
            userAvatarIm.setRoundAvatarWithBorder(user.avatar)
            curAvatarPath = user.avatar
        }
        userNameTx.text = user.name
        albumCountTx.text = user.albums.size.toString()
        cardCountTx.text = user.albums.filter { !it.isFavorite }
                .fold(0, { acc, album -> acc + album.photocards.size })
                .toString()
    }

    private var curImagePath = ""

    fun setPhotocard(photocard: PhotocardDto) {
        if (curImagePath != photocard.photo) {
            photo.setImage(photocard.photo)
            curImagePath = photocard.photo
        }
        cardNameTx.text = photocard.title
        if (flexbox.childCount == 0) {
            photocard.tags.forEach { flexbox.addView(TagView(context, it, null)) }
        }
    }

    fun setFavorite(favorite: Boolean) {
        favoriteIcon.visibility = if (favorite) View.VISIBLE else View.GONE
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


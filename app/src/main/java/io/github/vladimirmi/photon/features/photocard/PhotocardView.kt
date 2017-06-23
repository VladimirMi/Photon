package io.github.vladimirmi.photon.features.photocard

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.data.models.Photocard
import io.github.vladimirmi.photon.data.models.User
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.ui.TagView
import io.github.vladimirmi.photon.ui.setImage
import io.github.vladimirmi.photon.ui.setRoundAvatarWithBorder
import kotlinx.android.synthetic.main.screen_photocard.view.*

/**
 * Created by Vladimir Mikhalev 14.06.2017.
 */

class PhotocardView(context: Context, attrs: AttributeSet) : BaseView<PhotocardPresenter, PhotocardView>(context, attrs) {

    override fun initDagger(context: Context) {
        DaggerService.getComponent<PhotocardScreen.Component>(context).inject(this)
    }

    override fun initView() {
        user_avatar.setOnClickListener { presenter.showAuthor() }
    }

    private var curAvatarPath = ""
    fun setUser(user: User) {
        if (user.avatar != curAvatarPath) {
            setRoundAvatarWithBorder(user.avatar, user_avatar, 0f)
            curAvatarPath = user.avatar
        }
        user_name.text = user.name
        album_count.text = user.albumCount.toString()
        card_count.text = user.photocardCount.toString()
    }

    private var curImagePath = ""
    fun setPhotoCard(photocard: Photocard) {
        if (curImagePath != photocard.photo) {
            setImage(photocard.photo, photo)
            curImagePath = photocard.photo
        }
        card_name.text = photocard.title
        val flexbox = LayoutInflater.from(context).inflate(R.layout.view_search_tags, this, false)
        flexbox as ViewGroup
        photocard.tags.forEach {
            val tag = TagView(context, it.tag, null)
            flexbox.addView(tag)
        }
        photocard_tags_wrapper.addView(flexbox)
    }
}


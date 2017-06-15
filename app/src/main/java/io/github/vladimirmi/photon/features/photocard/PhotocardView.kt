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
    override fun onBackPressed(): Boolean {
        return false
    }

    override fun initDagger(context: Context) {
        DaggerService.getComponent<PhotocardScreen.Component>(context).inject(this)
    }

    override fun initView() {

    }

    override fun onViewRestored() {
        super.onViewRestored()
    }

    fun setUser(user: User) {
        setRoundAvatarWithBorder(user.avatar, author_avatar, 0f)
        author_nickname.text = user.name
        album_num.text = user.albumCount.toString()
        card_num.text = user.photocardCount.toString()
    }

    fun setPhotoCard(photocard: Photocard) {
        setImage(photocard.photo, photo)
        card_name.text = photocard.title
        val tagsContainer = LayoutInflater.from(context).inflate(R.layout.view_search_tags, photocard_tags_wrapper, false)
        tagsContainer as ViewGroup
        photocard.tags.forEach {
            val tag = TagView(context, it.tag, null)
            tagsContainer.addView(tag)
        }
        photocard_tags_wrapper.addView(tagsContainer)
    }
}


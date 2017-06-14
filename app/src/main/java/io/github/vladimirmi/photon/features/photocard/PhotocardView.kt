package io.github.vladimirmi.photon.features.photocard

import android.content.Context
import android.util.AttributeSet
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.data.models.Photocard
import io.github.vladimirmi.photon.data.models.User
import io.github.vladimirmi.photon.di.DaggerService
import timber.log.Timber

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
        Timber.e(user.toString())
    }

    fun setPhotoCard(photocard: Photocard) {
        Timber.e(photocard.toString())
    }
}


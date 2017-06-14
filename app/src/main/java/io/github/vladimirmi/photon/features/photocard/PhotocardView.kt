package io.github.vladimirmi.photon.features.photocard

import android.content.Context
import android.util.AttributeSet
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.di.DaggerService

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
        TODO("not implemented")
    }

    override fun onViewRestored() {
        super.onViewRestored()
    }
}


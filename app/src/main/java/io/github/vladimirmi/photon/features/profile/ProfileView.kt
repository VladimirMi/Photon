package io.github.vladimirmi.photon.features.profile

import android.content.Context
import android.util.AttributeSet
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.di.DaggerService

/**
 * Created by Vladimir Mikhalev 15.06.2017.
 */

class ProfileView(context: Context, attrs: AttributeSet)
    : BaseView<ProfilePresenter, ProfileView>(context, attrs) {

    override fun onBackPressed() = false

    override fun initDagger(context: Context) {
        DaggerService.getComponent<ProfileScreen.Component>(context).inject(this)
    }

    override fun initView() {

    }
}


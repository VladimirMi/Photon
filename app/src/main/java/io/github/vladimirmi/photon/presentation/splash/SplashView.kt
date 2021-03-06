package io.github.vladimirmi.photon.presentation.splash

import android.content.Context
import android.util.AttributeSet
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.di.DaggerService

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

class SplashView(context: Context, attrs: AttributeSet) :
        BaseView<SplashPresenter, SplashView>(context, attrs) {

    override fun initDagger(context: Context) {
        DaggerService.getComponent<SplashScreen.Component>(context).inject(this)
    }

    override fun initView() {}
}

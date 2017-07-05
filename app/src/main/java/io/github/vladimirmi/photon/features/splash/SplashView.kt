package io.github.vladimirmi.photon.features.splash

import android.content.Context
import android.util.AttributeSet
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.di.DaggerService
import kotlinx.android.synthetic.main.screen_splash.view.*
import timber.log.Timber

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

class SplashView(context: Context, attrs: AttributeSet) :
        BaseView<SplashPresenter, SplashView>(context, attrs) {

    override fun initDagger(context: Context) {
        DaggerService.getComponent<SplashScreen.Component>(context).inject(this)
    }

    override fun initView() {
        Timber.e("initView: show")
        loading.show()
    }
}

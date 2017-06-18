package io.github.vladimirmi.photon.features.splash

import flow.Direction
import flow.Flow
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.features.main.MainScreen
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.net.ConnectException

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

class SplashPresenter(model: ISplashModel, rootPresenter: RootPresenter) :
        BasePresenter<SplashView, ISplashModel>(model, rootPresenter) {

    override fun initToolbar() {
        rootPresenter.getNewToolbarBuilder()
                .setBottomMenuEnabled(false)
                .setToolbarVisible(false)
                .setBackGround(R.color.transparent)
                .build()
    }

    override fun initView(view: SplashView) {
        model.updateLimitPhotoCards(60, 3000)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({},
                        { handleError(it) },
                        { openMainScreen() })
    }

    private fun handleError(error: Throwable) {
        Timber.e(error.message)
        if (error is ConnectException) {
            view.showMessage(R.string.message_err_connect)
            openMainScreen()
        }
    }

    private fun openMainScreen() {
        Flow.get(view).replaceTop(MainScreen(), Direction.FORWARD)
    }
}

package io.github.vladimirmi.photon.features.splash

import flow.Direction
import flow.Flow
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.features.main.MainScreen
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.github.vladimirmi.photon.utils.AppConfig
import io.github.vladimirmi.photon.utils.ErrorObserver
import io.reactivex.disposables.Disposable
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
        compDisp.add(updatePhotos())
    }

    private fun updatePhotos(): Disposable {
        return model.updateLimitPhotoCards(AppConfig.SPLASH_UPDATE_PHOTOCARDS, AppConfig.SPLASH_TIMEOUT)
                .subscribeWith(object : ErrorObserver<Any>() {
                    override fun onComplete() {
                        openMainScreen()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        handleError(e)
                    }
                })
    }


    private fun handleError(error: Throwable) {
        if (error is ConnectException) {
            view.showMessage(R.string.message_err_connect)
        }
        if (model.dbIsNotEmpty()) openMainScreen()
    }

    private fun openMainScreen() {
        Flow.get(view).replaceTop(MainScreen(), Direction.FORWARD)
    }
}

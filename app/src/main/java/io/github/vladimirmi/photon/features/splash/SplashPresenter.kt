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
        var slowNet = false

        return model.updateLimitPhotoCards(AppConfig.PHOTOCARDS_PAGE_SIZE, AppConfig.SPLASH_TIMEOUT)
                .subscribeWith(object : ErrorObserver<Boolean>() {
                    override fun onNext(loaded: Boolean) {
                        if (slowNet) view.showMessage(R.string.message_err_connect)
                        if (!loaded) slowNet = true
                    }

                    override fun onComplete() {
                        openMainScreen()
                    }

                    override fun onError(e: Throwable) {
                        if (e is NoSuchElementException) {
                            openMainScreen() //304 empty
                            return
                        }
                        super.onError(e)
                        handleError(e)
                    }
                })
    }


    private fun handleError(error: Throwable) {
        rootPresenter.showMessage(R.string.message_api_err_unknown)
        if (model.dbIsNotEmpty()) openMainScreen()
    }

    private fun openMainScreen() {
        Flow.get(view).replaceTop(MainScreen(), Direction.FORWARD)
    }
}

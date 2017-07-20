package io.github.vladimirmi.photon.features.splash

import flow.Direction
import flow.Flow
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.features.main.MainScreen
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.github.vladimirmi.photon.utils.AppConfig
import io.github.vladimirmi.photon.utils.ErrorObserver
import io.github.vladimirmi.photon.utils.ErrorSingleObserver
import io.github.vladimirmi.photon.utils.observeOnMainThread
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

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
        var loaded = false

        val updateObs = model.updateLimitPhotoCards(AppConfig.PHOTOCARDS_PAGE_SIZE)
                .doOnNext { loaded = true }
                .doOnError { if (it is NoSuchElementException) loaded = true } //304 empty
                .map { false } // ended

        return Observable.mergeDelayError(
                Observable.timer(AppConfig.SPLASH_TIMEOUT, TimeUnit.MILLISECONDS).map { true }, // ended
                updateObs)
                .observeOnMainThread()
                .subscribeWith(object : ErrorObserver<Boolean>() {
                    override fun onNext(ended: Boolean) {
                        if (ended) {
                            if (loaded) {
                                openMainScreen()
                            } else {
                                view.showMessage(R.string.message_err_connect)
                                chooseCanOpen()
                            }
                        }
                    }

                    override fun onComplete() {
                        openMainScreen(AppConfig.PHOTOCARDS_PAGE_SIZE)
                    }

                    override fun onError(e: Throwable) {
                        if (loaded) {
                            openMainScreen(AppConfig.PHOTOCARDS_PAGE_SIZE)
                            return
                        }
                        super.onError(e)
                        handleError(e)
                    }
                })

    }


    private fun handleError(error: Throwable) {
        rootPresenter.showMessage(R.string.message_api_err_unknown)
        chooseCanOpen()
    }

    private fun chooseCanOpen() {
        compDisp.add(model.dbIsNotEmpty().subscribeWith(object : ErrorSingleObserver<Boolean>() {
            override fun onSuccess(notEmpty: Boolean) {
                if (notEmpty) openMainScreen()
            }
        }))
    }

    private fun openMainScreen(updated: Int = 0) {
        Flow.get(view).replaceTop(MainScreen(updated), Direction.FORWARD)
    }
}

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
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import timber.log.Timber
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
        rootPresenter.showLoading()
        compDisp.add(updatePhotos())
        if (!rootPresenter.isNetAvailable()) view.showError(R.string.message_err_net)
    }

    private fun updatePhotos(): Disposable {
        if (!rootPresenter.isNetAvailable()) {
            view.showNetError()
            chooseCanOpen()
        }

        val updateObs = model.updateAll(AppConfig.PHOTOCARDS_PAGE_SIZE)
                .toObservable<Long>()

        val timer = Observable.timer(AppConfig.SPLASH_TIMEOUT,
                TimeUnit.MILLISECONDS,
                AndroidSchedulers.mainThread())

        return Observable.mergeDelayError(timer, updateObs)
                .subscribeWith(object : ErrorObserver<Long>() {
                    override fun onNext(it: Long) {
                        Timber.e("onNext: timer ended")
                        chooseCanOpen()
                    }

                    override fun onComplete() {
                        openMainScreen(AppConfig.PHOTOCARDS_PAGE_SIZE)
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        handleError(e)
                    }
                })

    }

    private fun handleError(error: Throwable) {
        view.showError(R.string.message_api_err_unknown)
        chooseCanOpen()
    }

    private fun chooseCanOpen() {
        compDisp.add(model.dbIsNotEmpty()
                .subscribeWith(object : ErrorSingleObserver<Boolean>() {
                    override fun onSuccess(notEmpty: Boolean) {
                        if (notEmpty) openMainScreen()
                    }
                }))
    }

    private fun openMainScreen(updated: Int = 0) {
        rootPresenter.hideLoading()
        Flow.get(view).replaceTop(MainScreen(updated), Direction.FORWARD)
    }
}

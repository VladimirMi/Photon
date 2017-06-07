package io.github.vladimirmi.photon.features.splash

import flow.Flow
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.features.main.MainScreen
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

class SplashPresenter(model: ISplashModel, rootPresenter: RootPresenter) :
        BasePresenter<SplashView, ISplashModel>(model, rootPresenter) {


    override fun initView(view: SplashView) {
        model.updateLimitPhotoCards(60, 3000)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {}, { Flow.get(view).set(MainScreen()) })
    }
}
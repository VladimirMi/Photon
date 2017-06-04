package io.github.vladimirmi.photon.features.main

import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

/**
 * Developer Vladimir Mikhalev, 03.06.2017.
 */

class MainPresenter(model: IMainModel, rootPresenter: RootPresenter) :
        BasePresenter<MainView, IMainModel>(model, rootPresenter) {

    override fun initView(view: MainView) {
        compDisp.add(subscribeOnPhotocards())
    }

    private fun subscribeOnPhotocards(): Disposable? {
        return model.getPhotoCards()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ view.setData(it) })
    }
}



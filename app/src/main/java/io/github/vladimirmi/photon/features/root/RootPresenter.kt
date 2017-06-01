package io.github.vladimirmi.photon.features.root

import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.di.DaggerScope

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

@DaggerScope(RootActivity::class)
class RootPresenter(model: IRootModel) :
        BasePresenter<IRootView, IRootModel>(model) {


    override fun initView(view: IRootView) {
        // do something
    }
}

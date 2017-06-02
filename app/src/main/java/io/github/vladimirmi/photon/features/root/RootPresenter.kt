package io.github.vladimirmi.photon.features.root

import android.content.Context
import android.os.Bundle
import io.github.vladimirmi.photon.di.DaggerScope
import mortar.Presenter
import mortar.bundler.BundleService

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

@DaggerScope(RootActivity::class)
class RootPresenter(val model: IRootModel) :
        Presenter<IRootView>() {

    override fun extractBundleService(view: IRootView?): BundleService {
        return BundleService.getBundleService(view as Context)
    }


    override fun onLoad(savedInstanceState: Bundle?) {
        // do something
    }
}

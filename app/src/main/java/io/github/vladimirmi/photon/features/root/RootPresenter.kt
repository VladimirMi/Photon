package io.github.vladimirmi.photon.features.root

import android.content.Context
import android.os.Bundle
import io.github.vladimirmi.photon.di.DaggerScope
import mortar.MortarScope
import mortar.Presenter
import mortar.bundler.BundleService
import timber.log.Timber

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

@DaggerScope(RootActivity::class)
class RootPresenter(val model: IRootModel) :
        Presenter<IRootView>() {

    override fun extractBundleService(view: IRootView?): BundleService {
        return BundleService.getBundleService(view as Context)
    }


    override fun onEnterScope(scope: MortarScope?) {
        super.onEnterScope(scope)
        Timber.tag(javaClass.simpleName)
        Timber.d("onEnterScope: ${scope?.name}")
    }

    override fun onLoad(savedInstanceState: Bundle?) {
        super.onLoad(savedInstanceState)
        Timber.d("onLoad")
    }

    override fun dropView(view: IRootView) {
        super.dropView(view)
        Timber.tag(javaClass.simpleName)
        Timber.d("dropView")
    }

    override fun onExitScope() {
        super.onExitScope()
        Timber.tag(javaClass.simpleName)
        Timber.d("onExitScope")
    }

    fun hasActiveView() = hasView()
}

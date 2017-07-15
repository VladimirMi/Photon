package io.github.vladimirmi.photon.core

import android.os.Bundle
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.reactivex.disposables.CompositeDisposable
import mortar.MortarScope
import mortar.ViewPresenter
import timber.log.Timber

/**
 * Developer Vladimir Mikhalev, 30.05.2017
 */

abstract class BasePresenter<V : BaseView<*, V>, out M : IModel>
(protected val model: M, protected val rootPresenter: RootPresenter)
    : ViewPresenter<V>() {

    protected lateinit var compDisp: CompositeDisposable

    override fun onEnterScope(scope: MortarScope?) {
        super.onEnterScope(scope)
        Timber.tag(javaClass.simpleName)
        Timber.d("onEnterScope: ${scope?.name}")
    }

    override fun onLoad(savedInstanceState: Bundle?) {
        super.onLoad(savedInstanceState)
        compDisp = CompositeDisposable()
        initToolbar()
        initView(view)
        Timber.tag(javaClass.simpleName)
        Timber.d("onLoad")
        if (!rootPresenter.isNetAvailable()) view.showNetError()
    }

    override fun dropView(view: V) {
        rootPresenter.clearMenu()
        super.dropView(view)
        compDisp.clear()
        Timber.tag(javaClass.simpleName)
        Timber.d("dropView")
    }

    override fun onExitScope() {
        super.onExitScope()
        Timber.tag(javaClass.simpleName)
        Timber.d("onExitScope")
        DaggerService.appComponent.watcher().watch(this)
    }

    protected abstract fun initToolbar()

    protected abstract fun initView(view: V)
}

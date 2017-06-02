package io.github.vladimirmi.photon.core

import android.os.Bundle
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.reactivex.disposables.CompositeDisposable
import mortar.MortarScope
import mortar.ViewPresenter
import timber.log.Timber

/**
 * Developer Vladimir Mikhalev, 30.05.2017
 */

abstract class BasePresenter<V : BaseView<*, V>, M : IModel>
(protected var model: M, protected var rootPresenter: RootPresenter)
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
        initView(view)
        Timber.tag(javaClass.simpleName)
        Timber.d("onLoad")
    }

    override fun dropView(view: V) {
        super.dropView(view)
        if (!compDisp.isDisposed) compDisp.dispose()
        Timber.tag(javaClass.simpleName)
        Timber.d("dropView")
    }

    protected abstract fun initView(view: V)
}

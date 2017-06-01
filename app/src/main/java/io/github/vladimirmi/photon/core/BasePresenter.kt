package io.github.vladimirmi.photon.core

import io.github.vladimirmi.photon.features.root.RootPresenter
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

/**
 * Developer Vladimir Mikhalev, 30.05.2017
 */


abstract class BasePresenter<V : IView, M : IModel>
(protected var model: M, protected var rootPresenter: RootPresenter? = null)
    : IPresenter<V> {

    protected var compDisp = CompositeDisposable()
    var view: V? = null
        private set

    fun hasView(): Boolean = view != null

    override fun takeView(v: V) {
        Timber.tag(javaClass.simpleName)
        Timber.d("takeView")
        view = v
        initView(v)
    }

    override fun dropView() {
        Timber.tag(javaClass.simpleName)
        Timber.d("dropView")
        if (!compDisp.isDisposed) compDisp.dispose()
        view = null
    }

    protected abstract fun initView(view: V)
}

package io.github.vladimirmi.photon.core

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import io.github.vladimirmi.photon.flow.FlowLifecycles
import timber.log.Timber
import javax.inject.Inject

/**
 * Developer Vladimir Mikhalev, 30.05.117
 */

abstract class BaseView<V : IView, P : BasePresenter<V, *>>
(context: Context, attrs: AttributeSet) :
        FrameLayout(context, attrs),
        IView, FlowLifecycles.BackPressListener, FlowLifecycles.ViewLifecycleListener {

    @Inject protected lateinit var presenter: P

    init {
        @Suppress("LeakingThis")
        if (!isInEditMode) initDagger(context)
    }

    protected abstract fun initDagger(context: Context)

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (!isInEditMode) {
            initView()
        }
    }

    protected abstract fun initView()

    override fun onViewRestored() {
        @Suppress("UNCHECKED_CAST")
        presenter.takeView(this as V)
        Timber.tag(javaClass.simpleName)
        Timber.d("onViewRestored")
    }

    override fun onViewDestroyed(removedByFlow: Boolean) {
        presenter.dropView()
        Timber.tag(javaClass.simpleName)
        Timber.d("onViewDestroyed")
    }
}

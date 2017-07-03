package io.github.vladimirmi.photon.core

import android.content.Context
import android.support.design.widget.Snackbar
import android.util.AttributeSet
import android.widget.FrameLayout
import io.github.vladimirmi.photon.flow.FlowLifecycles
import timber.log.Timber
import javax.inject.Inject

/**
 * Developer Vladimir Mikhalev, 30.05.117
 */

abstract class BaseView<P : BasePresenter<V, *>, V : BaseView<P, V>>
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
        @Suppress("UNCHECKED_CAST")
        presenter.dropView(this as V)
        Timber.tag(javaClass.simpleName)
        Timber.d("onViewDestroyed by Flow $removedByFlow")
    }

    override fun onBackPressed(): Boolean = false

    override fun showMessage(string: String) {
        Snackbar.make(this, string, Snackbar.LENGTH_LONG).show()
    }

    override fun showMessage(stringId: Int) {
        Snackbar.make(this, stringId, Snackbar.LENGTH_LONG).show()
    }

    override fun showError(stringId: Int) {
        Snackbar.make(this, stringId, Snackbar.LENGTH_SHORT).show()
    }
}

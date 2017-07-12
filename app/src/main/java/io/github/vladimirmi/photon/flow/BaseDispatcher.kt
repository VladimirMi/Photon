package io.github.vladimirmi.photon.flow

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import flow.Dispatcher
import flow.Flow
import flow.Traversal
import flow.TraversalCallback

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

abstract class BaseDispatcher(val baseContext: Context) : Dispatcher,
        FlowLifecycles.BackPressListener, FlowLifecycles.ActivityResultListener,
        FlowLifecycles.PermissionRequestListener, FlowLifecycles.ViewLifecycleListener,
        FlowLifecycles.PreSaveViewStateListener, FlowLifecycles.StartStopListener {

    abstract override fun dispatch(traversal: Traversal, callback: TraversalCallback)

    lateinit var viewContainer: ViewGroup
    lateinit var activityContainer: ViewGroup

    fun getActiveView(): View? = viewContainer.getChildAt(0)

    override fun onStart() {
        FlowLifecycleProvider.onStart(getActiveView())
    }

    override fun onStop() {
        FlowLifecycleProvider.onStop(getActiveView())
    }

    override fun onViewRestored() {
        FlowLifecycleProvider.onViewRestored(getActiveView())
    }

    override fun onViewDestroyed(removedByFlow: Boolean) {
        FlowLifecycleProvider.onViewDestroyed(getActiveView(), removedByFlow)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        FlowLifecycleProvider.onActivityResult(getActiveView(), requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        FlowLifecycleProvider.onRequestPermissionsResult(getActiveView(), requestCode, permissions, grantResults)
    }

    override fun preSaveViewState() {
        FlowLifecycleProvider.preSaveViewState(getActiveView())
    }

    override fun onBackPressed(): Boolean {
        if (FlowLifecycleProvider.onBackPressed(getActiveView())) {
            return true
        }
        return Flow.get(baseContext).goBack()
    }

    fun <T> Traversal.getNewKey() = this.destination.top<T>()
    fun <T> Traversal.getPreviousKey() = this.origin?.top<T?>()

    fun View.restoreFromState(traversal: Traversal) {
        if (this is FlowLifecycles.ViewLifecycleListener) {
            onViewRestored()
        }
        val state = traversal.getState(Flow.getKey<Any>(context) ?: return)
        state.restore(this)
    }

    fun View.saveToState(traversal: Traversal) {
        if (this is FlowLifecycles.PreSaveViewStateListener) {
            preSaveViewState()
        }
        val state = traversal.getState(Flow.getKey<Any>(context) ?: return)
        state.save(this)
    }

    fun View.notifyRemoval() {
        if (this is FlowLifecycles.ViewLifecycleListener) {
            onViewDestroyed(removedByFlow = true)
        }
    }
}

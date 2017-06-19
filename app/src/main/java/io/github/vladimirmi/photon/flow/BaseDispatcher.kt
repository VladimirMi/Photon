package io.github.vladimirmi.photon.flow

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import flow.*

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

abstract class BaseDispatcher(val baseContext: Context) : Dispatcher,
        FlowLifecycles.BackPressListener, FlowLifecycles.ActivityResultListener,
        FlowLifecycles.PermissionRequestListener, FlowLifecycles.ViewLifecycleListener,
        FlowLifecycles.PreSaveViewStateListener, FlowLifecycles.StartStopListener {

    abstract override fun dispatch(traversal: Traversal, callback: TraversalCallback)

    var viewContainer: ViewGroup? = null

    fun getActiveView(): View? {
        return viewContainer?.getChildAt(0)
    }

    override fun onStart() {
        FlowLifecycleProvider.onStart(getActiveView() ?: return)
    }

    override fun onStop() {
        FlowLifecycleProvider.onStop(getActiveView() ?: return)
    }

    override fun onViewRestored() {
        FlowLifecycleProvider.onViewRestored(getActiveView() ?: return)
    }

    override fun onViewDestroyed(removedByFlow: Boolean) {
        FlowLifecycleProvider.onViewDestroyed(getActiveView() ?: return, removedByFlow)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        FlowLifecycleProvider.onActivityResult(getActiveView() ?: return, requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        FlowLifecycleProvider.onRequestPermissionsResult(getActiveView() ?: return, requestCode, permissions, grantResults)
    }

    override fun preSaveViewState() {
        FlowLifecycleProvider.preSaveViewState(getActiveView() ?: return)
    }

    override fun onBackPressed(): Boolean {
        if (FlowLifecycleProvider.onBackPressed(getActiveView() ?: return canGoBack())) {
            return true
        }
        return canGoBack()
    }

    private fun canGoBack() = Flow.get(baseContext).goBack()

    fun isPreviousKeySameAsNewKey(origin: History?, destination: History) =
            origin?.top<Any>() == destination.top<Any>()

    fun <T> getNewKey(traversal: Traversal): T = traversal.destination.top()

    fun <T> getPreviousKey(traversal: Traversal): T? = traversal.origin?.top()

    fun persistViewToStateAndNotifyRemoval(traversal: Traversal, view: View) {
        persistViewToState(traversal, view)
        notifyViewForFlowRemoval(view)
    }

    fun restoreViewFromState(traversal: Traversal, view: View?) {
        if (view != null) {
            if (view is FlowLifecycles.ViewLifecycleListener) {
                onViewRestored()
            }
            val incomingState = traversal.getState(Flow.getKey<Any>(view.context) ?: return)
            incomingState.restore(view)
        }
    }

    private fun persistViewToState(traversal: Traversal, view: View?) {
        if (view != null) {
            if (view is FlowLifecycles.PreSaveViewStateListener) {
                preSaveViewState()
            }
            val outgoingState = traversal.getState(Flow.getKey<Any>(view.context) ?: return)
            outgoingState.save(view)
        }
    }

    private fun notifyViewForFlowRemoval(view: View?) {
        if (view is FlowLifecycles.ViewLifecycleListener) {
            onViewDestroyed(true)
        }
    }
}

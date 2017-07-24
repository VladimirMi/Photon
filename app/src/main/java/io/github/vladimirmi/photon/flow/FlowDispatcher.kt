package io.github.vladimirmi.photon.flow

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.transitionseverywhere.TransitionManager
import flow.Direction
import flow.Flow
import flow.Traversal
import flow.TraversalCallback
import io.github.vladimirmi.photon.core.BaseScreen
import io.github.vladimirmi.photon.utils.prepareChangeScreenTransitionSet
import mortar.MortarScope

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

class FlowDispatcher<S : BaseScreen<*>>(baseContext: Context) : BaseDispatcher(baseContext) {

    override fun dispatch(traversal: Traversal, callback: TraversalCallback) {
        val previousScreen = traversal.getPreviousKey<S>()
        val newScreen = traversal.getNewKey<S>()
        if (previousScreen == newScreen) {
            callback.onTraversalCompleted()
            return
        }

        val flowContext = traversal.createContext(newScreen, baseContext)
        val mortarScope = Flow.getService<MortarScope>(newScreen.scopeName, flowContext)
        val mortarContext = mortarScope?.createContext(flowContext)

        val layoutInflater = LayoutInflater.from(mortarContext)

        val previousView = getActiveView()
        val newView = layoutInflater.inflate(newScreen.layoutResId, viewContainer, false)

        if (previousView != null) {
            prepareTransition(viewContainer, previousView, newView, traversal.direction)
            if (previousScreen != null && previousView.javaClass != newView.javaClass) {
                previousView.saveToState(traversal)
                previousView.notifyRemoval()
            }
            viewContainer.removeView(previousView)
        }

        viewContainer.addView(newView)
        newView.restoreFromState(traversal)

        callback.onTraversalCompleted()
    }

    private fun prepareTransition(container: ViewGroup,
                                  previousView: View,
                                  newView: View,
                                  direction: Direction) {
        if (direction == Direction.REPLACE) return

        val set = prepareChangeScreenTransitionSet(previousView, newView, direction).setStartDelay(200L)
        TransitionManager.beginDelayedTransition(container, set)
    }
}

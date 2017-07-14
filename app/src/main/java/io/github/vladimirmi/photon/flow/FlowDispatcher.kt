package io.github.vladimirmi.photon.flow

import android.content.Context
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.transitionseverywhere.Slide
import com.transitionseverywhere.TransitionManager
import com.transitionseverywhere.TransitionSet
import flow.Direction
import flow.Flow
import flow.Traversal
import flow.TraversalCallback
import io.github.vladimirmi.photon.core.BaseScreen
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

        val slideIn = Slide()
        val slideOut = Slide()

        slideIn.addTarget(newView)
        slideOut.addTarget(previousView)

        if (direction == Direction.FORWARD) {
            slideIn.slideEdge = Gravity.END
            slideOut.slideEdge = Gravity.START

        } else {
            slideIn.slideEdge = Gravity.START
            slideOut.slideEdge = Gravity.END
        }

        val set = TransitionSet()
        set.addTransition(slideIn)
                .addTransition(slideOut)
                .setDuration(300)
                .interpolator = FastOutSlowInInterpolator()

        TransitionManager.beginDelayedTransition(container, set)
    }
}

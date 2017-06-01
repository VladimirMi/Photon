package io.github.vladimirmi.photon.flow

import android.content.Context
import android.support.annotation.LayoutRes
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.transitionseverywhere.Slide
import com.transitionseverywhere.TransitionManager
import com.transitionseverywhere.TransitionSet
import flow.Direction
import flow.Traversal
import flow.TraversalCallback
import io.github.vladimirmi.photon.core.BaseScreen

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

class FlowDispatcher(baseContext: Context) : BaseDispatcher(baseContext) {

    var activityContainer: ViewGroup? = null

    override fun dispatch(traversal: Traversal, callback: TraversalCallback) {
        if (isPreviousKeySameAsNewKey(traversal.origin, traversal.destination)) {
            callback.onTraversalCompleted()
            return
        }

        val newKey: BaseScreen<*> = getNewKey(traversal)
        @LayoutRes val newScreenLayout = newKey.layoutResId
        val flowContext = traversal.createContext(newKey, baseContext)
        val layoutInflater = LayoutInflater.from(flowContext)

        val previousView = getActiveView()
        val newView = layoutInflater.inflate(newScreenLayout, viewContainer, false)

        if (previousView != null) {
            prepareTransition(activityContainer!!, previousView, newView, traversal.direction)

            if (traversal.origin != null && previousView.javaClass != newView.javaClass) {
                persistViewToStateAndNotifyRemoval(traversal, previousView)
            }
        }

        viewContainer?.removeView(previousView)

        viewContainer?.addView(newView)
        restoreViewFromState(traversal, newView)

        callback.onTraversalCompleted()
    }

    private fun prepareTransition(container: ViewGroup, previousView: View, newView: View, direction: Direction) {
        if (direction == Direction.REPLACE) {
            return
        }
        val slideIn = Slide()
        slideIn.addTarget(newView)

        val slideOut = Slide()
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

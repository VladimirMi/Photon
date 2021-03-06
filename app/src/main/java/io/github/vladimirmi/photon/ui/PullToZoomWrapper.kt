package io.github.vladimirmi.photon.ui

import android.content.Context
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import com.transitionseverywhere.ChangeBounds
import com.transitionseverywhere.TransitionManager
import com.transitionseverywhere.TransitionSet
import kotlinx.android.synthetic.main.screen_photocard.view.*


/**
 * Created by Vladimir Mikhalev 05.07.2017.
 */

class PullToZoomWrapper(context: Context, attrs: AttributeSet)
    : FrameLayout(context, attrs) {

    private val headerContainer by lazy { header_container }
    private val scrollView by lazy { scroll_view }

    private val FRICTION = 2.0f
    private val PARALLAX = 0.3f

    private var isPulled = false
    private var headerHeight = 0
    private var initialX = 0f
    private var initialY = 0f
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    private val scrollListener = { onScrollChange(scrollView.scrollY) }

    fun subscribe() {
        scrollView.setOnTouchListener { v, event ->
            v.onTouchEvent(event)
            if (onInterceptTouchEvent(event)) onTouchEvent(event)
            true
        }
        scrollView.viewTreeObserver.addOnScrollChangedListener(scrollListener)
    }

    fun unsubscribe() {
        scrollView.setOnTouchListener(null)
        scrollView.viewTreeObserver.removeOnScrollChangedListener(scrollListener)
    }

    private fun onScrollChange(scrollY: Int) {
        val scroll = (PARALLAX * scrollY).toInt()
        headerContainer.scrollTo(0, -scroll)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) headerHeight = headerContainer.height
    }

    private fun isReadyForPull() = scrollView.scrollY == 0

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        var intercepted = false

        if (isPulled) {
            intercepted = true
        } else {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = event.x
                    initialY = event.y
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isReadyForPull()) {
                        val xDelta = event.x - initialX
                        val yDelta = event.y - initialY
                        if (yDelta > 0 && Math.abs(yDelta) > touchSlop && Math.abs(yDelta) > Math.abs(xDelta)) {
                            intercepted = true
                            initialX = event.x
                            initialY = event.y
                        }
                    }
                }
            }
        }
        return intercepted
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                isPulled = true
                pullTo(event.y)
                return true
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                isPulled = false
                restoreInitialSize()
                return true
            }
        }
        return false
    }


    private fun pullTo(y: Float) {
        val delta = Math.round(Math.max(y - initialY, 0f) / FRICTION)

        headerContainer.layoutParams = headerContainer.layoutParams.apply {
            height = headerHeight + delta
        }
    }

    private fun restoreInitialSize() {
        val set = TransitionSet()
        set.addTransition(ChangeBounds())
                .setDuration(300)
                .interpolator = FastOutSlowInInterpolator()
        TransitionManager.beginDelayedTransition(this, set)

        headerContainer.layoutParams = headerContainer.layoutParams.apply {
            height = headerHeight
        }
    }
}



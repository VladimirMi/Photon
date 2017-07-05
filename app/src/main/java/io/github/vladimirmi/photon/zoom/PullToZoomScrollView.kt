package io.github.vladimirmi.photon.zoom

import android.content.Context
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.animation.Interpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import io.github.vladimirmi.photon.R
import timber.log.Timber

/**
 * Created by Vladimir Mikhalev 05.07.2017.
 */

class PullToZoomScrollView(context: Context, attrs: AttributeSet)
    : ScrollView(context, attrs) {

    private val rootContainer by lazy { LinearLayout(context) }
    private val headerContainer by lazy { FrameLayout(context) }
    private var headerView: View? = null
    private var zoomView: View? = null
    private var contentView: View? = null

    private var isParallax = true
    private var headerHeight = 0
    private val runnable = ScalingRunnable()

    init {
        parseAttrs(context, attrs)
        combineViews()
    }

    private fun parseAttrs(context: Context, attrs: AttributeSet) {
        val inflater = LayoutInflater.from(context)
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.PullToZoomView, 0, 0)

        val headerLayout = a.getResourceId(R.styleable.PullToZoomView_headerView, 0)
        if (headerLayout != 0) headerView = inflater.inflate(headerLayout, null, false)

        val zoomLayout = a.getResourceId(R.styleable.PullToZoomView_zoomView, 0)
        if (headerLayout != 0) zoomView = inflater.inflate(zoomLayout, null, false)

        val contentLayout = a.getResourceId(R.styleable.PullToZoomView_contentView, 0)
        if (headerLayout != 0) contentView = inflater.inflate(contentLayout, null, false)
        a.recycle()
    }

    private fun combineViews() {
        rootContainer.orientation = LinearLayout.VERTICAL

        zoomView?.let { headerContainer.addView(it) }
        headerView?.let { headerContainer.addView(it) }

        rootContainer.addView(headerContainer)
        contentView?.let { rootContainer.addView(it) }

//        headerContainer.clipChildren = false
//        rootContainer.clipChildren = false

        addView(rootContainer, LayoutParams(MATCH_PARENT, MATCH_PARENT))
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            headerHeight = headerContainer.height
            Timber.e("onLayout: headerHeight=$headerHeight")
        }
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        if (isParallax) {
            Timber.e("onScrollChanged: y = $scrollY")
            val f = (headerHeight - headerContainer.bottom + scrollY).toFloat()
            Timber.e("onScrollChanged: f = $f")
            if (f > 0.0f && f < headerHeight) {
                val i = (0.65 * f).toInt()
                headerContainer.scrollTo(0, -i)
            } else if (headerContainer.scrollY != 0) {
                headerContainer.scrollTo(0, 0)
            }
        }
    }


    internal inner class ScalingRunnable : Runnable {
        private var duration: Long = 0L
        var isFinished = true
            private set
        private var scale: Float = 0f
        private var startTime: Long = 0L

        fun abortAnimation() {
            isFinished = true
        }

        override fun run() {
            if (zoomView != null) {
                val f2: Float
                val localLayoutParams: ViewGroup.LayoutParams
                if (!isFinished && scale > 1.0) {
                    val f1 = (SystemClock.currentThreadTimeMillis().toFloat() - startTime.toFloat()) / duration.toFloat()
                    f2 = scale - (scale - 1.0f) * interpolator.getInterpolation(f1)
                    localLayoutParams = headerContainer.layoutParams
                    Timber.e("run: f2 = $f2")
                    if (f2 > 1.0f) {
                        localLayoutParams.height = (f2 * headerHeight).toInt()
                        headerContainer.layoutParams = localLayoutParams
//                        if (isCustomHeaderHeight) {
//                            val zoomLayoutParams = zoomView.getLayoutParams()
//                            zoomLayoutParams.height = (f2 * headerHeight).toInt()
//                            zoomView.setLayoutParams(zoomLayoutParams)
//                        }
                        post(this)
                        return
                    }
                    isFinished = true
                }
            }
        }

        fun startAnimation(duration: Long) {
            if (zoomView != null) {
                startTime = SystemClock.currentThreadTimeMillis()
                this.duration = duration
                scale = headerContainer.bottom.toFloat() / headerHeight
                isFinished = false
                post(this)
            }
        }
    }

    private val interpolator = Interpolator { input: Float ->
        val f = input - 1.0f
        return@Interpolator 1.0f + f * (f * (f * (f * f)))
    }

    private var isPulled = false
    private var lastX = 0f
    private var lastY = 0f
    private var initialY = 0f
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        var intercept = false

        when (event.action) {
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                intercept = false
                isPulled = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (isPulled) intercept = true
                if (isReadyForPull()) {
                    val xDiff = event.x - lastX
                    val yDiff = event.y - lastY

                    if (Math.abs(yDiff) > touchSlop && Math.abs(yDiff) > Math.abs(xDiff)) {
                        lastY = event.y
                        intercept = true
                    }
                }
            }
        }
        return intercept
    }

    private fun isReadyForPull(): Boolean {
        return scrollY == 0
    }

    private var isZooming = false

    override fun onTouchEvent(event: MotionEvent): Boolean {
//        if (event.action == MotionEvent.ACTION_DOWN && event.edgeFlags != 0) {
//            return false
//        }

        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                lastY = event.y
                pullEvent()
                isZooming = true
                isPulled = true
                return true
            }

            MotionEvent.ACTION_DOWN -> {
                if (isReadyForPull()) {
                    initialY = event.y
                    lastY = initialY
                    return true
                }
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                if (isPulled) {
                    isPulled = false
                    if (isZooming) {
                        smoothScrollToTop()
                        isZooming = false
                        return true
                    }
                    return true
                }
            }
        }
        return false
    }

    private fun smoothScrollToTop() {
        runnable.startAnimation(200L)
    }

    private val FRICTION = 2.0f

    private fun pullEvent() {
        val newScrollValue = Math.round(Math.min(initialY - lastY, 0f) / FRICTION)
        Timber.e("pullEvent: to $newScrollValue")
        pullHeaderToZoom(newScrollValue)
    }

    private fun pullHeaderToZoom(newScrollValue: Int) {
        Log.d("ScrollView", "pullHeaderToZoom --> newScrollValue = " + newScrollValue)
        if (!runnable.isFinished) {
            runnable.abortAnimation()
        }

        val localLayoutParams = headerContainer.layoutParams
        localLayoutParams.height = Math.abs(newScrollValue) + headerHeight
        headerContainer.layoutParams = localLayoutParams

    }
}



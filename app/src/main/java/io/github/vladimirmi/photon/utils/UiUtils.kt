package io.github.vladimirmi.photon.utils

import android.content.Context
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.transitionseverywhere.Slide
import com.transitionseverywhere.TransitionSet
import flow.Direction
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.IView
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.ui.CircleTransformation


/**
 * Created by Vladimir Mikhalev 31.05.2017.
 */

fun ImageView.setImage(path: String?) {
    Glide.with(context)
            .load(path)
            .centerCrop()
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.RESULT)
            .placeholder(R.drawable.placeholder_gradient)
            .crossFade()
            .into(this)
}

fun ImageView.setRoundAvatarWithBorder(path: String?, border: Float = 0f) {
    Glide.with(context)
            .load(path)
            .bitmapTransform(CircleTransformation(context, border))
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.RESULT)
            .placeholder(R.drawable.ic_placeholder_avatar)
            .crossFade()
            .into(this)
}

val Context.dp get() = getDisplayMetrics().density

val Context.sp get() = getDisplayMetrics().scaledDensity

fun Context.getDisplayMetrics(): DisplayMetrics {
    val displayMetrics = DisplayMetrics()
    (this.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics
}

@Suppress("UNCHECKED_CAST")
inline fun <V : IView> IView.afterNetCheck(block: V.() -> Unit) {
    val netAvail = DaggerService.appComponent.dataManager().isNetworkAvailable().blockingFirst()
    if (netAvail) block(this as V) else showNetError()
}

@Suppress("UNCHECKED_CAST")
inline fun <V : IView> IView.afterAuthCheck(block: V.() -> Unit) {
    val profileId = DaggerService.appComponent.dataManager().getProfileId()
    if (profileId.isNotEmpty()) block(this as V) else showAuthError()
}

inline fun View.waitForMeasure(crossinline block: () -> Unit) {
    if (width > 0 && height > 0) {
        block()
        return
    }
    viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            val observer = viewTreeObserver
            if (observer.isAlive) {
                observer.removeOnPreDrawListener(this)
            }
            block()
            return true
        }
    })
}

fun prepareChangeScreenTransitionSet(previousView: View,
                                     newView: View,
                                     direction: Direction): TransitionSet {
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

    return TransitionSet().addTransition(slideIn)
            .addTransition(slideOut)
            .setDuration(AppConfig.CHANGE_SCREEN_ANIMATION)
            .setInterpolator(FastOutSlowInInterpolator())
}


package io.github.vladimirmi.photon.utils

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
            .placeholder(R.drawable.ic_placeholder_image)
            .into(this)
}

fun ImageView.setRoundAvatarWithBorder(path: String?, border: Float) {
    Glide.with(context)
            .load(path)
            .bitmapTransform(CircleTransformation(context, border))
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.RESULT)
            .placeholder(R.drawable.ic_placeholder_avatar)
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
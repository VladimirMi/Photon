package io.github.vladimirmi.photon.utils

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

/**
 * Created by Vladimir Mikhalev 31.05.2017.
 */

fun setImage(path: String, view: ImageView) {
    Glide.with(view.context)
            .load(path)
            .fitCenter()
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.RESULT)
            .into(view)
}

fun getDensity(context: Context): Float {
    return getDisplayMetrics(context).density
}

fun getScaledDensity(context: Context): Float {
    return getDisplayMetrics(context).scaledDensity
}

fun getDisplayMetrics(context: Context): DisplayMetrics {
    val displayMetrics = DisplayMetrics()
    (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics
}

fun Context.getColor() {}

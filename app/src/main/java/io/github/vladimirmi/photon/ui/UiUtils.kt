package io.github.vladimirmi.photon.ui

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import io.github.vladimirmi.photon.R

/**
 * Created by Vladimir Mikhalev 31.05.2017.
 */

fun setImage(path: String, view: ImageView) {
    Glide.with(view.context)
            .load(path)
            .centerCrop()
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.RESULT)
            .placeholder(R.drawable.ic_placeholder_image)
            .into(view)
}

fun setRoundAvatarWithBorder(path: String, view: ImageView, border: Float) {
    Glide.with(view.context)
            .load(path)
            .bitmapTransform(CircleTransformation(view.context, border))
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.RESULT)
            .placeholder(R.drawable.ic_placeholder_avatar)
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
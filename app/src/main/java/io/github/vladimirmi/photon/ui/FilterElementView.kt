package io.github.vladimirmi.photon.ui

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.TextView
import io.github.vladimirmi.photon.R


/**
 * Created by Vladimir Mikhalev 11.06.2017.
 */

class FilterElementView(context: Context, attrs: AttributeSet?) : TextView(context, attrs) {

    private var picked = false
    private val drawable: Drawable = DrawableCompat.wrap(compoundDrawables[1])
    private val colorAccent = ContextCompat.getColor(context, R.color.color_accent)
    private val colorGrey = ContextCompat.getColor(context, R.color.grey)
    private var color = colorGrey
    val query get() = Pair((parent as ViewGroup).tag as String, tag as String)

    init {
        if (drawable is GradientDrawable) {
            val a = context.theme.obtainStyledAttributes(attrs, R.styleable.FilterElementView, 0, 0)
            val shapeColor = a.getColor(R.styleable.FilterElementView_solidColor, 0)
            a.recycle()
            drawable.setColor(shapeColor)
        }
        setupDrawable(drawable)
    }

    private fun setupDrawable(drawable: Drawable) {
        color = if (picked) colorAccent else colorGrey
        if (drawable is GradientDrawable) {
            setupShapeDrawable(drawable)
        } else {
            setupVectorDrawable(drawable)
        }
    }

    private fun setupVectorDrawable(vectorDrawable: Drawable) {
        DrawableCompat.setTint(vectorDrawable, color)
        DrawableCompat.setTintMode(vectorDrawable, PorterDuff.Mode.SRC_IN)
        setTextColor(color)
    }

    private fun setupShapeDrawable(shapeDrawable: GradientDrawable) {
        shapeDrawable.setStroke(5, color)
        setTextColor(color)
    }

    fun pick() {
        picked = !picked
        setupDrawable(drawable)
    }

    fun setAction(filterAction: (FilterElementView) -> Unit) {
        setOnClickListener {
            run(filterAction)
            pick()
        }
    }
}
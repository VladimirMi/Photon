package io.github.vladimirmi.photon.ui

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.TextView
import io.github.vladimirmi.photon.R
import timber.log.Timber


/**
 * Created by Vladimir Mikhalev 11.06.2017.
 */

class FilterElementView(context: Context, attrs: AttributeSet?) : TextView(context, attrs) {

    var picked = false
        private set
    var radioMode: Boolean
    private val drawable: Drawable = DrawableCompat.wrap(compoundDrawables[1])
    private val colorAccent = ContextCompat.getColor(context, R.color.color_accent)
    private val colorGrey = ContextCompat.getColor(context, R.color.grey)
    private var color = colorGrey
    val filter get() = Pair("filters." + (parent as ViewGroup).tag as String, tag as String)

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.FilterElementView, 0, 0)
        val shapeColor = a.getColor(R.styleable.FilterElementView_solidColor, 0)
        radioMode = a.getBoolean(R.styleable.FilterElementView_radioMode, false)
        a.recycle()

        if (drawable is GradientDrawable) {
            drawable.setColor(shapeColor)
        }
        setupDrawable()
    }

    private fun setupDrawable() {
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
        Timber.e("pick: $picked radio $radioMode")
        picked = !picked
        setupDrawable()
        if (radioMode && picked) {
            val parent = parent as ViewGroup
            (0..parent.childCount - 1)
                    .map { parent.getChildAt(it) as FilterElementView }
                    .filter { it.picked && it.tag != tag }
                    .forEach { it.pick() }
        }
    }

    fun setAction(filterAction: (FilterElementView) -> Unit) {
        setOnClickListener {
            pick()
            run(filterAction)
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val state = Bundle()
        state.putParcelable("SUPER", super.onSaveInstanceState())
        state.putBoolean("PICKED", picked)
        return state
    }


    override fun onRestoreInstanceState(state: Parcelable) {
        state as Bundle
        picked = state.getBoolean("PICKED", false)
        setupDrawable()
        val superState = state.getParcelable<Parcelable>("SUPER")
        super.onRestoreInstanceState(superState)
    }
}
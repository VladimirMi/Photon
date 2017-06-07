package io.github.vladimirmi.photon.utils

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.flexbox.FlexboxLayout
import io.github.vladimirmi.photon.R

class TagView(context: Context, tag: String, val runnable: (TagView) -> Unit) :
        TextView(context) {
    private var picked = false

    init {
        setBackgroundResource(R.drawable.btn_tag)
        setTextColor(ContextCompat.getColor(context, R.color.black))
        setOnClickListener({
            pick()
            run(runnable)
        })
        val padding: Int = (getDensity(context) * 4).toInt()
        setPadding(padding, padding, padding, padding)
        text = tag
    }

    override fun setLayoutParams(params: ViewGroup.LayoutParams?) {
        if (params is FlexboxLayout.LayoutParams) {
            val margin: Int = (getDensity(context) * 4).toInt()
            params.setMargins(margin, margin, margin, margin)
        }
        super.setLayoutParams(params)
    }

    fun pick() {
        picked = !picked
        if (picked) {
            setBackgroundResource(R.drawable.btn_tag_accent)
            setTextColor(ContextCompat.getColor(context, R.color.color_accent))
        } else {
            setBackgroundResource(R.drawable.btn_tag)
            setTextColor(ContextCompat.getColor(context, R.color.black))
        }
    }
}
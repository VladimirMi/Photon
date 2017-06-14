package io.github.vladimirmi.photon.features.search.tags

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.flexbox.FlexboxLayout
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.utils.getDensity

class TagView(context: Context, tag: String, val action: ((TagView) -> Unit)?) :
        TextView(context) {
    private var picked = false

    override fun getText(): CharSequence {
        return super.getText().removePrefix("#")
    }

    init {
        if (action != null) {
            setOnClickListener({
                pick()
                run(action)
            })
        }
        val padding: Int = (getDensity(context) * 4).toInt()
        setPadding(padding, padding, padding, padding)
        text = "#" + tag
        id = tag.hashCode()
        setupView()
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
        setupView()
    }

    private fun setupView() {
        if (picked) {
            setBackgroundResource(R.drawable.btn_tag_accent)
            setTextColor(ContextCompat.getColor(context, R.color.color_accent))
        } else {
            setBackgroundResource(R.drawable.btn_tag)
            setTextColor(ContextCompat.getColor(context, R.color.black))
        }
    }
}
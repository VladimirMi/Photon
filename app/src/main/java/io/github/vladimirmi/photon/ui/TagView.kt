package io.github.vladimirmi.photon.ui

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.flexbox.FlexboxLayout
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.utils.dp

class TagView(context: Context, tag: String, val action: ((TagView) -> Unit)?) :
        TextView(context) {
    var picked = false
        private set

    override fun getText() = super.getText().removePrefix("#")

    init {
        if (action != null) {
            setOnClickListener({
                pick()
                run(action)
            })
        }
        val padding = (8 * context.dp).toInt()
        setPadding(padding, padding, padding, padding)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        text = if (tag.startsWith("#")) tag else "#$tag"
        id = tag.hashCode()
        setupView()
    }

    override fun setLayoutParams(params: ViewGroup.LayoutParams?) {
        if (params is FlexboxLayout.LayoutParams) {
            val margin = (6 * context.dp).toInt()
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
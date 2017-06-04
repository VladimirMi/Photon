package io.github.vladimirmi.photon.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

/**
 * Created by Vladimir Mikhalev 31.05.2017.
 */

object EmptyTextWatcher : TextWatcher {
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {}

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
}

open class SimpleTextWatcher : TextWatcher by EmptyTextWatcher

fun EditText.onTextChangedX(function: (String) -> Unit): Unit {
    this.addTextChangedListener(object : SimpleTextWatcher() {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            function(s.toString())
        }
    })
}

fun setImage(path: String, view: ImageView) {
    Glide.with(view.context)
            .load(path)
            .fitCenter()
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.RESULT)
            .into(view)
}

package io.github.vladimirmi.photon.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

/**
 * Created by Vladimir Mikhalev 31.05.2017.
 */
abstract class SimpleTextWatcher : TextWatcher {
    override fun afterTextChanged(s: Editable?) {
        // do nothing
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // do nothing
    }
}


fun EditText.onTextChangedX(function: (String) -> Unit): Unit {
    this.addTextChangedListener(object : SimpleTextWatcher() {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            function(s.toString())
        }
    })
}
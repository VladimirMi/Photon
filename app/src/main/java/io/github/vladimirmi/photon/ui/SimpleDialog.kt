package io.github.vladimirmi.photon.ui

import android.view.ViewGroup
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseDialog
import kotlinx.android.synthetic.main.dialog_simple.view.*

/**
 * Created by Vladimir Mikhalev 27.06.2017.
 */

class SimpleDialog(viewGroup: ViewGroup, stringId: Int, action: () -> Unit)
    : BaseDialog(R.layout.dialog_simple, viewGroup) {

    private val message = view.dialog_message
    private val ok = view.ok
    private val cancel = view.cancel

    init {
        message.setText(stringId)
        cancel.setOnClickListener { hide() }
        ok.setOnClickListener { action() }
    }
}
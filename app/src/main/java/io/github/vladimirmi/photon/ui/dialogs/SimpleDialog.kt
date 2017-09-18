package io.github.vladimirmi.photon.ui.dialogs

import android.view.ViewGroup
import io.github.vladimirmi.photon.R
import kotlinx.android.synthetic.main.dialog_simple.view.*

/**
 * Created by Vladimir Mikhalev 27.06.2017.
 */

class SimpleDialog(viewGroup: ViewGroup, stringId: Int, action: () -> Unit)
    : BaseDialog(R.layout.dialog_simple, viewGroup) {

    private val message = dialogView.dialog_message
    private val ok = dialogView.ok
    private val cancel = dialogView.cancel

    init {
        message.setText(stringId)
        cancel.setOnClickListener { hide() }
        ok.setOnClickListener { action() }
    }
}
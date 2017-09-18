package io.github.vladimirmi.photon.ui.dialogs

import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by Vladimir Mikhalev 27.06.2017.
 */

open class BaseDialog(layoutId: Int, viewGroup: ViewGroup) {

    val dialogView: View = LayoutInflater.from(viewGroup.context).inflate(layoutId, null, false)
    private val dialog: AlertDialog

    init {
        dialog = AlertDialog.Builder(viewGroup.context)
                .setView(dialogView)
                .create()
    }

    open fun show() = dialog.show()

    open fun hide() = dialog.dismiss()
}
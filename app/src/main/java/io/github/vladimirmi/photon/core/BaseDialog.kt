package io.github.vladimirmi.photon.core

import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by Vladimir Mikhalev 27.06.2017.
 */

open class BaseDialog(layoutId: Int, val viewGroup: ViewGroup) {

    val view: View = LayoutInflater.from(viewGroup.context).inflate(layoutId, null, false)
    protected val dialog: AlertDialog

    init {
        dialog = AlertDialog.Builder(viewGroup.context)
                .setView(view)
                .create()
    }

    fun show() {
        dialog.show()
    }

    fun hide() {
        dialog.dismiss()
    }
}
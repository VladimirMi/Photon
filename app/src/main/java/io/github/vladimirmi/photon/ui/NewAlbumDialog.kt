package io.github.vladimirmi.photon.ui

import android.view.ViewGroup
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.data.models.NewAlbumReq
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import kotlinx.android.synthetic.main.dialog_new_album.view.*

/**
 * Created by Vladimir Mikhalev 23.06.2017.
 */

class NewAlbumDialog(viewGroup: ViewGroup, newAlbumAction: (NewAlbumReq) -> Unit)
    : ValidationDialog(R.layout.dialog_new_album, viewGroup) {

    val name = view.name
    val description = view.description
    val ok = view.ok
    val cancel = view.cancel

    init {
        listenFields()
        cancel.setOnClickListener { hide() }
        ok.setOnClickListener {
            val request = NewAlbumReq(title = name.text.toString(),
                    description = description.text.toString()
            )
            newAlbumAction(request)
        }
    }

    private fun listenFields() {
        val nameObs = getValidObs(name, NAME_PATTERN, view.name_error, view.context.getString(R.string.message_err_name))
        val descriptionObs = getValidObs(description, DESCRIPTION_PATTERN, view.description_error, view.context.getString(R.string.message_err_description))

        Observable.combineLatest(nameObs, descriptionObs,
                BiFunction { t1: Boolean, t2: Boolean -> t1 && t2 })
                .subscribe { ok.isEnabled = it }
    }
}
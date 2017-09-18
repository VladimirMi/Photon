package io.github.vladimirmi.photon.ui.dialogs

import android.view.ViewGroup
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.data.models.realm.Album
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.dialog_new_album.view.*

/**
 * Created by Vladimir Mikhalev 23.06.2017.
 */

class NewAlbumDialog(viewGroup: ViewGroup, newAlbumAction: (Album) -> Unit)
    : ValidationDialog(R.layout.dialog_new_album, viewGroup) {

    private val name = dialogView.name
    private val description = dialogView.description
    private val ok = dialogView.ok
    private val cancel = dialogView.cancel

    init {
        cancel.setOnClickListener { hide() }
        ok.setOnClickListener {
            val request = Album(title = name.text.toString(),
                    description = description.text.toString()
            )
            newAlbumAction(request)
        }
    }

    override fun listenFields(): Disposable {
        val nameObs = name.validate(NAME_PATTERN, dialogView.name_error,
                dialogView.context.getString(R.string.message_err_name))
        val descriptionObs = description.validate(DESCRIPTION_PATTERN, dialogView.description_error,
                dialogView.context.getString(R.string.message_err_description))

        return validateForm(listOf(nameObs, descriptionObs))
                .subscribe { ok.isEnabled = it }
    }
}
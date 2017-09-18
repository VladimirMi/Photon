package io.github.vladimirmi.photon.ui.dialogs

import android.view.ViewGroup
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.data.models.req.AlbumEditReq
import io.github.vladimirmi.photon.domain.models.AlbumDto
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.dialog_new_album.view.*

/**
 * Created by Vladimir Mikhalev 23.06.2017.
 */

class EditAlbumDialog(
        viewGroup: ViewGroup,
        editAlbumAction: (AlbumEditReq) -> Unit,
        albumDto: AlbumDto)
    : ValidationDialog(R.layout.dialog_edit_album, viewGroup) {

    private val nameField = dialogView.name
    private val descriptionField = dialogView.description
    private val ok = dialogView.ok
    private val cancel = dialogView.cancel

    init {
        nameField.setText(albumDto.title)
        nameField.setSelection(albumDto.title.length)
        descriptionField.setText(albumDto.description)
        descriptionField.setSelection(albumDto.description.length)
        cancel.setOnClickListener { hide() }
        ok.setOnClickListener {
            val request = AlbumEditReq(
                    id = albumDto.id,
                    title = nameField.text.toString(),
                    description = descriptionField.text.toString()
            )
            editAlbumAction(request)
        }
    }

    override fun listenFields(): Disposable {
        val nameObs = nameField.validate(NAME_PATTERN, dialogView.name_error,
                dialogView.context.getString(R.string.message_err_name))
        val descriptionObs = descriptionField.validate(DESCRIPTION_PATTERN, dialogView.description_error,
                dialogView.context.getString(R.string.message_err_description))

        return validateForm(listOf(nameObs, descriptionObs))
                .subscribe { ok.isEnabled = it }
    }
}
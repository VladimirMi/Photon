package io.github.vladimirmi.photon.ui

import android.view.ViewGroup
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import kotlinx.android.synthetic.main.dialog_new_album.view.*

/**
 * Created by Vladimir Mikhalev 23.06.2017.
 */

class EditAlbumDialog(
        viewGroup: ViewGroup,
        editAlbumAction: (AlbumDto) -> Unit,
        albumDto: AlbumDto)
    : ValidationDialog(R.layout.dialog_edit_album, viewGroup) {

    private val nameField = view.name
    private val descriptionField = view.description
    private val ok = view.ok
    private val cancel = view.cancel

    init {
        nameField.setText(albumDto.title)
        nameField.setSelection(albumDto.title.length)
        descriptionField.setText(albumDto.description)
        descriptionField.setSelection(albumDto.description.length)
        cancel.setOnClickListener { hide() }
        ok.setOnClickListener {
            val request = AlbumDto(
                    id = albumDto.id,
                    title = nameField.text.toString(),
                    description = descriptionField.text.toString()
            )
            editAlbumAction(request)
        }
    }

    fun subscribe() = compDisp.add(listenFields())

    fun unsubscribe() = compDisp.clear()

    private fun listenFields(): Disposable {
        val nameObs = getValidObs(nameField, NAME_PATTERN, view.name_error, view.context.getString(R.string.message_err_name))
        val descriptionObs = getValidObs(descriptionField, DESCRIPTION_PATTERN, view.description_error, view.context.getString(R.string.message_err_description))

        return Observable.combineLatest(nameObs, descriptionObs,
                BiFunction { t1: Boolean, t2: Boolean -> t1 && t2 })
                .subscribe { ok.isEnabled = it }
    }
}
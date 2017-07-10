package io.github.vladimirmi.photon.ui

import android.view.ViewGroup
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.data.models.NewAlbumReq
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function3
import kotlinx.android.synthetic.main.dialog_new_album.view.*

/**
 * Created by Vladimir Mikhalev 23.06.2017.
 */

class NewAlbumDialog(viewGroup: ViewGroup, newAlbumAction: (NewAlbumReq) -> Unit)
    : ValidationDialog(R.layout.dialog_new_album, viewGroup) {

    private val name = view.name
    private val description = view.description
    private val ok = view.ok
    private val cancel = view.cancel

    init {
        cancel.setOnClickListener { hide() }
        ok.setOnClickListener {
            val request = NewAlbumReq(title = name.text.toString(),
                    description = description.text.toString()
            )
            newAlbumAction(request)
        }
    }

    fun subscribe() = compDisp.add(listenFields())

    fun unsubscribe() = compDisp.clear()

    private fun listenFields(): Disposable {
        val nameObs = getValidObs(name, NAME_PATTERN, view.name_error, view.context.getString(R.string.message_err_name))
        val descriptionObs = getValidObs(description, DESCRIPTION_PATTERN, view.description_error, view.context.getString(R.string.message_err_description))
        val netObs = getNetObs(view.context.getString(R.string.message_err_net))

        return Observable.combineLatest(nameObs, descriptionObs, netObs,
                Function3 { t1: Boolean, t2: Boolean, t3: Boolean -> t1 && t2 && t3 })
                .startWith(false)
                .subscribe { ok.isEnabled = it }
    }
}
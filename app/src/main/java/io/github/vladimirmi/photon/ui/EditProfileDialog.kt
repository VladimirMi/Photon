package io.github.vladimirmi.photon.ui

import android.view.ViewGroup
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.data.models.req.EditProfileReq
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import kotlinx.android.synthetic.main.dialog_edit_profile.view.*

/**
 * Created by Vladimir Mikhalev 17.06.2017.
 */

class EditProfileDialog(viewGroup: ViewGroup, val editProfileAction: (EditProfileReq) -> Unit)
    : ValidationDialog(R.layout.dialog_edit_profile, viewGroup) {

    private val loginField = view.login
    private val nameField = view.name
    private val ok = view.ok
    private val cancel = view.cancel

    init {
        cancel.setOnClickListener { hide() }
        ok.setOnClickListener {
            kotlin.run {
                val request = EditProfileReq(login = loginField.text.toString(),
                        name = nameField.text.toString()
                )
                editProfileAction(request)
            }
        }
    }

    fun subscribe() = compDisp.add(listenFields())

    fun unsubscribe() = compDisp.clear()

    private fun listenFields(): Disposable {
        val loginObs = getValidObs(loginField, LOGIN_PATTERN, view.login_error, view.context.getString(R.string.message_err_login))
        val nameObs = getValidObs(nameField, NAME_PATTERN, view.name_error, view.context.getString(R.string.message_err_name))

        return Observable.combineLatest(loginObs, nameObs,
                BiFunction { t1: Boolean, t2: Boolean -> t1 && t2 })
                .subscribe { ok.isEnabled = it }
    }

    fun initFields(login: CharSequence, name: CharSequence) {
        loginField.setText(login)
        loginField.setSelection(login.length)
        nameField.setText(name)
        nameField.setSelection(name.length)
    }
}


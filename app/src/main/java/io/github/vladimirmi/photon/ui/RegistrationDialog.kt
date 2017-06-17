package io.github.vladimirmi.photon.ui

import android.view.ViewGroup
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.data.models.SignUpReq
import io.reactivex.Observable
import io.reactivex.functions.Function5
import kotlinx.android.synthetic.main.dialog_registration.view.*

/**
 * Created by Vladimir Mikhalev 15.06.2017.
 */

class RegistrationDialog(viewGroup: ViewGroup, val registrationAction: (SignUpReq) -> Unit)
    : ValidationDialog(R.layout.dialog_registration, viewGroup) {

    val login = view.login
    val email = view.email
    val name = view.name
    val password = view.password
    val ok = view.ok
    val cancel = view.cancel

    init {
        listenFields()
        cancel.setOnClickListener { dialog.cancel() }
        ok.setOnClickListener {
            kotlin.run {
                val request = SignUpReq(login = login.text.toString(),
                        email = email.text.toString(),
                        name = name.text.toString(),
                        password = password.text.toString()
                )
                registrationAction(request)
            }
        }
    }

    private fun listenFields() {
        val loginObs = getValidObs(login, LOGIN_PATTERN, view.login_error, view.context.getString(R.string.message_err_login))
        val emailObs = getValidObs(email, EMAIL_PATTERN, view.email_error, view.context.getString(R.string.message_err_email))
        val nameObs = getValidObs(name, NAME_PATTERN, view.name_error, view.context.getString(R.string.message_err_name))
        val passwordObs = getValidObs(password, PASSWORD_PATTERN, view.password_error, view.context.getString(R.string.message_err_password))
        val netObs = getNetObs(view.context.getString(R.string.message_err_net))

        Observable.combineLatest(loginObs, emailObs, nameObs, passwordObs, netObs,
                Function5 { t1: Boolean, t2: Boolean, t3: Boolean, t4: Boolean, t5: Boolean -> t1 && t2 && t3 && t4 && t5 })
                .subscribe {
                    ok.isEnabled = it
                }
    }
}
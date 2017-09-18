package io.github.vladimirmi.photon.ui.dialogs

import android.view.ViewGroup
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.data.models.req.SignUpReq
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.dialog_registration.view.*

/**
 * Created by Vladimir Mikhalev 15.06.2017.
 */

class RegistrationDialog(viewGroup: ViewGroup, val registrationAction: (SignUpReq) -> Unit)
    : ValidationDialog(R.layout.dialog_registration, viewGroup) {

    private val login = dialogView.login
    private val email = dialogView.email
    private val name = dialogView.name
    private val password = dialogView.password
    private val ok = dialogView.ok
    private val cancel = dialogView.cancel

    init {
        cancel.setOnClickListener { hide() }
        ok.setOnClickListener {
            val request = SignUpReq(login = login.text.toString(),
                    email = email.text.toString(),
                    name = name.text.toString(),
                    password = password.text.toString()
            )
            registrationAction(request)
        }
    }

    override fun listenFields(): Disposable {
        val loginObs = login.validate(LOGIN_PATTERN, dialogView.login_error,
                dialogView.context.getString(R.string.message_err_login))
        val emailObs = email.validate(EMAIL_PATTERN, dialogView.email_error,
                dialogView.context.getString(R.string.message_err_email))
        val nameObs = name.validate(NAME_PATTERN, dialogView.name_error,
                dialogView.context.getString(R.string.message_err_name))
        val passwordObs = password.validate(PASSWORD_PATTERN, dialogView.password_error,
                dialogView.context.getString(R.string.message_err_password))

        return validateForm(listOf(loginObs, emailObs, nameObs, passwordObs))
                .subscribe { ok.isEnabled = it }
    }
}
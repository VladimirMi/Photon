package io.github.vladimirmi.photon.ui.dialogs

import android.view.ViewGroup
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.data.models.req.SignInReq
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.dialog_login.view.*

/**
 * Created by Vladimir Mikhalev 17.06.2017.
 */

class LoginDialog(viewGroup: ViewGroup, loginAction: (SignInReq) -> Unit)
    : ValidationDialog(R.layout.dialog_login, viewGroup) {

    private val email = dialogView.email
    private val password = dialogView.password
    private val ok = dialogView.ok
    private val cancel = dialogView.cancel

    init {
        cancel.setOnClickListener { hide() }
        ok.setOnClickListener {
            kotlin.run {
                val request = SignInReq(
                        email = email.text.toString(),
                        password = password.text.toString()
                )
                loginAction(request)
            }
        }
    }

    override fun listenFields(): Disposable {
        val emailObs = email.validate(EMAIL_PATTERN, dialogView.email_error,
                dialogView.context.getString(R.string.message_err_email))
        val passwordObs = password.validate(PASSWORD_PATTERN, dialogView.password_error,
                dialogView.context.getString(R.string.message_err_password))

        return validateForm(listOf(emailObs, passwordObs))
                .subscribe { ok.isEnabled = it }
    }
}


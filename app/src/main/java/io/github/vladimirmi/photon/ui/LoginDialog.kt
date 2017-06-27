package io.github.vladimirmi.photon.ui

import android.view.ViewGroup
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.data.models.SignInReq
import io.reactivex.Observable
import io.reactivex.functions.Function3
import kotlinx.android.synthetic.main.dialog_login.view.*

/**
 * Created by Vladimir Mikhalev 17.06.2017.
 */

class LoginDialog(viewGroup: ViewGroup, val loginAction: (SignInReq) -> Unit)
    : ValidationDialog(R.layout.dialog_login, viewGroup) {

    val email = view.email
    val password = view.password
    val ok = view.ok
    val cancel = view.cancel

    init {
        listenFields()
        cancel.setOnClickListener { hide() }
        ok.setOnClickListener {
            kotlin.run {
                val request = SignInReq(email = email.text.toString(),
                        password = password.text.toString()
                )
                loginAction(request)
            }
        }
    }

    private fun listenFields() {
        val emailObs = getValidObs(email, EMAIL_PATTERN, view.email_error, view.context.getString(R.string.message_err_email))
        val passwordObs = getValidObs(password, PASSWORD_PATTERN, view.password_error, view.context.getString(R.string.message_err_password))
        val netObs = getNetObs(view.context.getString(R.string.message_err_net))

        Observable.combineLatest(emailObs, passwordObs, netObs,
                Function3 { t1: Boolean, t2: Boolean, t3: Boolean -> t1 && t2 && t3 })
                .subscribe {
                    ok.isEnabled = it
                }
    }
}


package io.github.vladimirmi.photon.ui

import android.view.ViewGroup
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.data.models.SignUpReq
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function5
import kotlinx.android.synthetic.main.dialog_registration.view.*

/**
 * Created by Vladimir Mikhalev 15.06.2017.
 */

class RegistrationDialog(viewGroup: ViewGroup, val registrationAction: (SignUpReq) -> Unit)
    : ValidationDialog(R.layout.dialog_registration, viewGroup) {

    private val login = view.login
    private val email = view.email
    private val name = view.name
    private val password = view.password
    private val ok = view.ok
    private val cancel = view.cancel

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

    fun subscribe() = compDisp.add(listenFields())

    fun unsubscribe() = compDisp.clear()

    private fun listenFields(): Disposable {
        val loginObs = getValidObs(login, LOGIN_PATTERN, view.login_error, view.context.getString(R.string.message_err_login))
        val emailObs = getValidObs(email, EMAIL_PATTERN, view.email_error, view.context.getString(R.string.message_err_email))
        val nameObs = getValidObs(name, NAME_PATTERN, view.name_error, view.context.getString(R.string.message_err_name))
        val passwordObs = getValidObs(password, PASSWORD_PATTERN, view.password_error, view.context.getString(R.string.message_err_password))
        val netObs = getNetObs(view.context.getString(R.string.message_err_net))

        return Observable.combineLatest(loginObs, emailObs, nameObs, passwordObs, netObs,
                Function5 { t1: Boolean, t2: Boolean, t3: Boolean, t4: Boolean, t5: Boolean -> t1 && t2 && t3 && t4 && t5 })
                .startWith(false)
                .subscribe {
                    ok.isEnabled = it
                }
    }
}
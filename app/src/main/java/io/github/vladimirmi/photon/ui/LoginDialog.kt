package io.github.vladimirmi.photon.ui

import android.view.ViewGroup
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.data.models.req.SignInReq
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function3
import kotlinx.android.synthetic.main.dialog_login.view.*

/**
 * Created by Vladimir Mikhalev 17.06.2017.
 */

class LoginDialog(viewGroup: ViewGroup, val loginAction: (SignInReq) -> Unit)
    : ValidationDialog(R.layout.dialog_login, viewGroup) {

    private val email = view.email
    private val password = view.password
    private val ok = view.ok
    private val cancel = view.cancel

    init {
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

    fun subscribe() = compDisp.add(listenFields())

    fun unsubscribe() = compDisp.clear()

    private fun listenFields(): Disposable {
        val emailObs = getValidObs(email, EMAIL_PATTERN, view.email_error, view.context.getString(R.string.message_err_email))
        val passwordObs = getValidObs(password, PASSWORD_PATTERN, view.password_error, view.context.getString(R.string.message_err_password))
        val netObs = getNetObs(view.context.getString(R.string.message_err_net))

        return Observable.combineLatest(emailObs, passwordObs, netObs,
                Function3 { t1: Boolean, t2: Boolean, t3: Boolean -> t1 && t2 && t3 })
                .startWith(false)
                .subscribe {
                    ok.isEnabled = it
                }
    }
}


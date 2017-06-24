package io.github.vladimirmi.photon.features.auth

import android.content.Context
import android.util.AttributeSet
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.data.models.SignInReq
import io.github.vladimirmi.photon.data.models.SignUpReq
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.ui.LoginDialog
import io.github.vladimirmi.photon.ui.RegistrationDialog
import kotlinx.android.synthetic.main.screen_auth.view.*

/**
 * Created by Vladimir Mikhalev 24.06.2017.
 */

class AuthView(context: Context, attrs: AttributeSet)
    : BaseView<AuthPresenter, AuthView>(context, attrs) {

    val registrationAction: (SignUpReq) -> Unit = { presenter.register(it) }
    val loginAction: (SignInReq) -> Unit = { presenter.login(it) }

    val registrationDialog = RegistrationDialog(this, registrationAction)
    val loginDialog = LoginDialog(this, loginAction)

    override fun initDagger(context: Context) {
        DaggerService.getComponent<AuthScreen.Component>(context).inject(this)
    }

    override fun initView() {
        login_btn.setOnClickListener { openLoginDialog() }
        registration_btn.setOnClickListener { openRegistrationDialog() }
    }

    fun openRegistrationDialog() = registrationDialog.dialog.show()

    fun openLoginDialog() = loginDialog.dialog.show()

    fun closeRegistrationDialog() = registrationDialog.dialog.cancel()

    fun closeLoginDialog() = loginDialog.dialog.cancel()
}
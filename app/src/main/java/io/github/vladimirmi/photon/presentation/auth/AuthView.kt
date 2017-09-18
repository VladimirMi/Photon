package io.github.vladimirmi.photon.presentation.auth

import android.content.Context
import android.util.AttributeSet
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.data.models.req.SignInReq
import io.github.vladimirmi.photon.data.models.req.SignUpReq
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.ui.dialogs.LoginDialog
import io.github.vladimirmi.photon.ui.dialogs.RegistrationDialog
import kotlinx.android.synthetic.main.screen_auth.view.*

/**
 * Created by Vladimir Mikhalev 24.06.2017.
 */

class AuthView(context: Context, attrs: AttributeSet)
    : BaseView<AuthPresenter, AuthView>(context, attrs) {

    private val registrationAction: (SignUpReq) -> Unit = { presenter.register(it) }
    private val loginAction: (SignInReq) -> Unit = { presenter.login(it) }

    private val registrationDialog = RegistrationDialog(this, registrationAction)
    private val loginDialog = LoginDialog(this, loginAction)

    override fun initDagger(context: Context) {
        DaggerService.getComponent<AuthScreen.Component>(context).inject(this)
    }

    override fun initView() {
        login_btn.setOnClickListener { presenter.startLogin() }
        registration_btn.setOnClickListener { presenter.startRegistration() }
    }

    fun openRegistrationDialog() = registrationDialog.show()
    fun closeRegistrationDialog() = registrationDialog.hide()
    fun openLoginDialog() = loginDialog.show()
    fun closeLoginDialog() = loginDialog.hide()

    fun setTitle(stringId: Int) = title.setText(stringId)
}
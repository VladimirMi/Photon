package io.github.vladimirmi.photon.features.auth

import android.content.Context
import android.util.AttributeSet
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.data.models.req.SignInReq
import io.github.vladimirmi.photon.data.models.req.SignUpReq
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.ui.LoginDialog
import io.github.vladimirmi.photon.ui.RegistrationDialog
import io.github.vladimirmi.photon.utils.afterNetCheck
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

    private val loginBtn by lazy { login_btn }
    private val registrationBtn by lazy { registration_btn }
    private val titleTxt by lazy { title }

    override fun initView() {
        loginBtn.setOnClickListener { afterNetCheck<AuthView> { openLoginDialog() } }
        registrationBtn.setOnClickListener { afterNetCheck<AuthView> { openRegistrationDialog() } }
    }

    fun openRegistrationDialog() = registrationDialog.show()
    fun closeRegistrationDialog() = registrationDialog.hide()
    fun openLoginDialog() = loginDialog.show()
    fun closeLoginDialog() = loginDialog.hide()

    override fun onViewRestored() {
        super.onViewRestored()
        registrationDialog.subscribe()
        loginDialog.subscribe()
    }

    override fun onViewDestroyed(removedByFlow: Boolean) {
        super.onViewDestroyed(removedByFlow)
        registrationDialog.unsubscribe()
        loginDialog.unsubscribe()
    }

    fun setTitle(stringId: Int) = titleTxt.setText(stringId)
}
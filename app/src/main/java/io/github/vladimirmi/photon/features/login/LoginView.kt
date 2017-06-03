package io.github.vladimirmi.photon.features.login

import android.content.Context
import android.util.AttributeSet
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.data.models.LoginDto
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.utils.onTextChangedX
import kotlinx.android.synthetic.main.screen_login.view.*

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

class LoginView(context: Context, attrs: AttributeSet) :
        BaseView<LoginPresenter, LoginView>(context, attrs), ILoginView {

    override fun initDagger(context: Context) {
        DaggerService.getComponent<LoginScreen.Component>(context).inject(this)
    }

    override fun initView() {
        // do something
    }

    override fun onViewRestored() {
        super.onViewRestored()
        username.onTextChangedX {
            presenter.updateUsername(it)
            loginDto.username = it
        }
        password.onTextChangedX {
            presenter.updatePassword(it)
            loginDto.password = it
        }
        submit.setOnClickListener { presenter.login() }
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    private lateinit var loginDto: LoginDto

    fun setup(loginDto: LoginDto) {
        this.loginDto = loginDto
        username.setText(loginDto.username)
        password.setText(loginDto.password)
    }
}

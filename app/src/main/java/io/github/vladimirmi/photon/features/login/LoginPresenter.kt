package io.github.vladimirmi.photon.features.login

import flow.Flow
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.data.models.LoginDto
import io.github.vladimirmi.photon.features.root.RootPresenter
import timber.log.Timber

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

class LoginPresenter(model: ILoginModel, rootPresenter: RootPresenter) :
        BasePresenter<LoginView, ILoginModel>(model, rootPresenter) {

    private lateinit var loginDto: LoginDto

    override fun initView(view: LoginView) {
        loginDto = Flow.getKey<LoginScreen>(view)?.loginDto as LoginDto
        view.setup(loginDto)
    }

    fun updateUsername(string: String) {
        Timber.d("username update with $string")
    }

    fun updatePassword(string: String) {
        Timber.d("password update with $string")
    }

    fun login() {
        Timber.d("login")
    }
}

package io.github.vladimirmi.photon.features.login

import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.features.root.RootPresenter
import timber.log.Timber

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

class LoginPresenter(model: ILoginModel, rootPresenter: RootPresenter) :
        BasePresenter<ILoginView, ILoginModel>(model, rootPresenter) {

    override fun initView(view: ILoginView) {
        // do something
    }

    fun updateUsername(string: String) {
        Timber.d("username update")
    }

    fun updatePassword(toString: String) {
        Timber.d("password update")
    }

    fun login() {
        Timber.d("login")
    }
}

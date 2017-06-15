package io.github.vladimirmi.photon.ui

import android.view.ViewGroup
import io.github.vladimirmi.photon.R
import io.reactivex.Observable
import io.reactivex.functions.Function4
import kotlinx.android.synthetic.main.dialog_registration.view.*

/**
 * Created by Vladimir Mikhalev 15.06.2017.
 */

class RegistrationDialog(viewGroup: ViewGroup)
    : ValidationDialog(R.layout.dialog_registration, viewGroup) {

    init {
        listenFields()
    }

    private fun listenFields() {
        val loginObs = getValidObs(view.login, LOGIN_PATTERN, view.login_error, "Шеф, все пропало!")
        val emailObs = getValidObs(view.email, EMAIL_PATTERN, view.email_error, "Шеф, все пропало!")
        val nameObs = getValidObs(view.name, NAME_PATTERN, view.name_error, "Шеф, все пропало!")
        val passwordObs = getValidObs(view.password, PASSWORD_PATTERN, view.password_error, "Шеф, все пропало!")

        Observable.combineLatest(loginObs, emailObs, nameObs, passwordObs,
                Function4 { t1: Boolean, t2: Boolean, t3: Boolean, t4: Boolean -> t1 && t2 && t3 && t4 })
                .subscribe { view.ok.isEnabled = it }
    }
}
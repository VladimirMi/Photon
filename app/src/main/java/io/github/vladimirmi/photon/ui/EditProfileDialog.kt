package io.github.vladimirmi.photon.ui

import android.view.ViewGroup
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.data.models.req.ProfileEditReq
import io.github.vladimirmi.photon.domain.models.UserDto
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import kotlinx.android.synthetic.main.dialog_edit_profile.view.*

/**
 * Created by Vladimir Mikhalev 17.06.2017.
 */

class EditProfileDialog(viewGroup: ViewGroup,
                        private val editProfileAction: (ProfileEditReq) -> Unit,
                        private val userDto: UserDto)
    : ValidationDialog(R.layout.dialog_edit_profile, viewGroup) {

    private val loginField = view.login
    private val nameField = view.name
    private val ok = view.ok
    private val cancel = view.cancel

    init {
        loginField.setText(userDto.login)
        nameField.setText(userDto.name)
        loginField.setSelection(userDto.login.length)
        nameField.setSelection(userDto.name.length)
        cancel.setOnClickListener { hide() }
        ok.setOnClickListener {
            val request = ProfileEditReq(
                    login = loginField.text.toString(),
                    name = nameField.text.toString(),
                    avatar = userDto.avatar
            )
            editProfileAction(request)
        }
    }

    fun subscribe() = compDisp.add(listenFields())

    //todo добавить в хайд
    fun unsubscribe() = compDisp.clear()

    private fun listenFields(): Disposable {
        val loginObs = getValidObs(loginField, LOGIN_PATTERN, view.login_error, view.context.getString(R.string.message_err_login))
        val nameObs = getValidObs(nameField, NAME_PATTERN, view.name_error, view.context.getString(R.string.message_err_name))

        return Observable.combineLatest(loginObs, nameObs,
                BiFunction { t1: Boolean, t2: Boolean -> t1 && t2 })
                .subscribe { ok.isEnabled = it }
    }
}


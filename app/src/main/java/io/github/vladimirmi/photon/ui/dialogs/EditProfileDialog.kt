package io.github.vladimirmi.photon.ui.dialogs

import android.view.ViewGroup
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.data.models.req.ProfileEditReq
import io.github.vladimirmi.photon.domain.models.UserDto
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.dialog_edit_profile.view.*

/**
 * Created by Vladimir Mikhalev 17.06.2017.
 */

class EditProfileDialog(viewGroup: ViewGroup,
                        editProfileAction: (ProfileEditReq) -> Unit,
                        userDto: UserDto)
    : ValidationDialog(R.layout.dialog_edit_profile, viewGroup) {

    private val loginField = dialogView.login
    private val nameField = dialogView.name
    private val ok = dialogView.ok
    private val cancel = dialogView.cancel

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

    override fun listenFields(): Disposable {
        val loginObs = loginField.validate(LOGIN_PATTERN, dialogView.login_error,
                dialogView.context.getString(R.string.message_err_login))
        val nameObs = nameField.validate(NAME_PATTERN, dialogView.name_error,
                dialogView.context.getString(R.string.message_err_name))

        return validateForm(listOf(loginObs, nameObs))
                .subscribe { ok.isEnabled = it }
    }
}


package io.github.vladimirmi.photon.features.main

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.util.AttributeSet
import flow.Flow
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.data.models.Photocard
import io.github.vladimirmi.photon.data.models.SignInReq
import io.github.vladimirmi.photon.data.models.SignUpReq
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.features.photocard.PhotocardScreen
import io.github.vladimirmi.photon.ui.LoginDialog
import io.github.vladimirmi.photon.ui.RegistrationDialog
import kotlinx.android.synthetic.main.screen_main.view.*


/**
 * Developer Vladimir Mikhalev, 03.06.2017.
 */

class MainView(context: Context, attrs: AttributeSet) :
        BaseView<MainPresenter, MainView>(context, attrs) {

    val cardAction: (Photocard) -> Unit = { showPhotoCard(it) }
    val adapter = MainAdapter(cardAction)

    val registrationAction: (SignUpReq) -> Unit = { register(it) }
    val loginAction: (SignInReq) -> Unit = { login(it) }

    val registrationDialog = RegistrationDialog(this, registrationAction)
    val loginDialog = LoginDialog(this, loginAction)

    override fun initDagger(context: Context) {
        DaggerService.getComponent<MainScreen.Component>(context).inject(this)
    }

    override fun initView() {
        photocard_list.layoutManager = GridLayoutManager(context, 2)
        photocard_list.adapter = adapter
    }

    override fun onBackPressed(): Boolean = false

    fun setData(data: List<Photocard>) = adapter.updateData(data)

    fun showPhotoCard(photocard: Photocard) = Flow.get(this).set(PhotocardScreen(photocard))

    fun openRegistrationDialog() = registrationDialog.dialog.show()

    fun openLoginDialog() = loginDialog.dialog.show()

    fun closeRegistrationDialog() = registrationDialog.dialog.cancel()

    fun closeLoginDialog() = loginDialog.dialog.cancel()

    fun showMessage(errorResId: Int) {
        if (registrationDialog.dialog.isShowing) registrationDialog.showMessage(errorResId) else
            loginDialog.showMessage(errorResId)
    }

    private fun register(req: SignUpReq) = presenter.register(req)
    private fun login(req: SignInReq) = presenter.login(req)
}

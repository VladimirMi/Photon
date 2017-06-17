package io.github.vladimirmi.photon.features.main

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.util.AttributeSet
import flow.Flow
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.data.models.LoginReq
import io.github.vladimirmi.photon.data.models.Photocard
import io.github.vladimirmi.photon.data.models.RegistrationReq
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

    override fun initDagger(context: Context) {
        DaggerService.getComponent<MainScreen.Component>(context).inject(this)
    }

    override fun initView() {
        recycler_view.layoutManager = GridLayoutManager(context, 2)
        recycler_view.adapter = adapter
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    fun setData(data: List<Photocard>) {
        adapter.updateData(data)
    }

    fun showPhotoCard(photocard: Photocard) {
        Flow.get(this).set(PhotocardScreen(photocard))
    }

    fun openLoginDialog() {
        LoginDialog(this, loginAction).dialog.show()
    }

    fun openRegistrationDialog() {
        RegistrationDialog(this, registrationAction).dialog.show()
    }

    val registrationAction: (RegistrationReq) -> Unit = { register(it) }
    val loginAction: (LoginReq) -> Unit = { login(it) }

    private fun register(req: RegistrationReq) {
        presenter.register(req)
    }

    private fun login(req: LoginReq) {
        presenter.login(req)
    }
}

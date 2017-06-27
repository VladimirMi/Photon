package io.github.vladimirmi.photon.features.main

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.widget.GridLayoutManager
import android.util.AttributeSet
import flow.Flow
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.data.models.SignInReq
import io.github.vladimirmi.photon.data.models.SignUpReq
import io.github.vladimirmi.photon.data.models.realm.Photocard
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
    val adapter = CardAdapter(cardAction)

    val registrationAction: (SignUpReq) -> Unit = { register(it) }
    val loginAction: (SignInReq) -> Unit = { login(it) }

    val registrationDialog = RegistrationDialog(this, registrationAction)
    val loginDialog = LoginDialog(this, loginAction)

    private var scroll = 0
    val state = Flow.getKey<MainScreen>(context)!!.state

    override fun initDagger(context: Context) {
        DaggerService.getComponent<MainScreen.Component>(context).inject(this)
    }

    override fun initView() {
        photocard_list.layoutManager = GridLayoutManager(context, 2)
        photocard_list.adapter = adapter
    }

    fun setData(data: List<Photocard>) {
        adapter.updateData(data)
        if (scroll != 0) {
            photocard_list.scrollToPosition(scroll)
            scroll = 0
        }
    }

    fun showPhotoCard(photocard: Photocard) = Flow.get(this).set(PhotocardScreen(photocard))

    fun openRegistrationDialog() = registrationDialog.show()

    fun openLoginDialog() = loginDialog.show()

    fun closeRegistrationDialog() = registrationDialog.hide()

    fun closeLoginDialog() = loginDialog.hide()

    private fun register(req: SignUpReq) = presenter.register(req)

    private fun login(req: SignInReq) = presenter.login(req)

    override fun onViewRestored() {
        super.onViewRestored()
        restoreHierarchyState(state)
    }

    override fun onViewDestroyed(removedByFlow: Boolean) {
        super.onViewDestroyed(removedByFlow)
        saveHierarchyState(state)
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable("SUPER", super.onSaveInstanceState())
        val lm = photocard_list.layoutManager as GridLayoutManager
        bundle.putInt("SCROLL", lm.findFirstVisibleItemPosition())
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val bundle = state as Bundle
        super.onRestoreInstanceState(bundle.getParcelable("SUPER"))
        scroll = bundle.getInt("SCROLL")
    }
}

package io.github.vladimirmi.photon.features.main

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.util.AttributeSet
import flow.Flow
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.data.models.SignInReq
import io.github.vladimirmi.photon.data.models.SignUpReq
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.ui.LoginDialog
import io.github.vladimirmi.photon.ui.RegistrationDialog
import kotlinx.android.synthetic.main.screen_main.view.*

/**
 * Developer Vladimir Mikhalev, 03.06.2017.
 */

class MainView(context: Context, attrs: AttributeSet) :
        BaseView<MainPresenter, MainView>(context, attrs) {

    private val cardAction: (Photocard) -> Unit = { presenter.showPhotoCard(it) }
    private val adapter = CardAdapter(cardAction)

    private val registrationAction: (SignUpReq) -> Unit = { register(it) }
    private val loginAction: (SignInReq) -> Unit = { login(it) }

    private val registrationDialog = RegistrationDialog(this, registrationAction)
    private val loginDialog = LoginDialog(this, loginAction)

    private var scroll = 0
    private val state = Flow.getKey<MainScreen>(context)!!.state

    override fun initDagger(context: Context) {
        DaggerService.getComponent<MainScreen.Component>(context).inject(this)
    }

    override fun initView() {
        photocard_list.layoutManager = GridLayoutManager(context, 2)
        photocard_list.adapter = adapter
    }

    fun setData(data: List<Photocard>) {
        adapter.updateData(data.filter { it.active })
        if (scroll != 0) {
            photocard_list.scrollToPosition(scroll)
            scroll = 0
        }
    }

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

    fun showFilterWarning() {
        val snackbar = Snackbar.make(this, R.string.main_message_filter, Snackbar.LENGTH_LONG)
                .setAction(R.string.main_message_filter_action, { presenter.resetFilter() })
        snackbar.view.setBackgroundColor(ContextCompat.getColor(context, R.color.text_color))
        snackbar.show()
    }
}

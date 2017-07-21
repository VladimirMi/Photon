package io.github.vladimirmi.photon.features.main

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import flow.Flow
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.data.models.SignInReq
import io.github.vladimirmi.photon.data.models.SignUpReq
import io.github.vladimirmi.photon.data.models.dto.PhotocardDto
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.ui.EndlessRecyclerViewScrollListener
import io.github.vladimirmi.photon.ui.LoginDialog
import io.github.vladimirmi.photon.ui.RegistrationDialog
import kotlinx.android.synthetic.main.screen_main.view.*

/**
 * Developer Vladimir Mikhalev, 03.06.2017.
 */

class MainView(context: Context, attrs: AttributeSet) :
        BaseView<MainPresenter, MainView>(context, attrs) {

    private val cardAction: (PhotocardDto) -> Unit = { presenter.showPhotoCard(it) }
    private val adapter = CardAdapter(cardAction)

    private val registrationAction: (SignUpReq) -> Unit = { register(it) }
    private val loginAction: (SignInReq) -> Unit = { login(it) }

    private val registrationDialog = RegistrationDialog(this, registrationAction)
    private val loginDialog = LoginDialog(this, loginAction)

    private var scrollPosition = 0
    private val state = Flow.getKey<MainScreen>(context)!!.state

    override fun initDagger(context: Context) {
        DaggerService.getComponent<MainScreen.Component>(context).inject(this)
    }

    private val photocardList by lazy { photocard_list }

    override fun initView() {
        photocardList.layoutManager = GridLayoutManager(context, 2)
        photocardList.adapter = adapter
        photocardList.addOnScrollListener(object : EndlessRecyclerViewScrollListener(photocardList.layoutManager as GridLayoutManager) {
            override fun onLoadMore(page: Int, limit: Int, view: RecyclerView) {
                presenter.loadMore(page, limit)
            }
        })
    }

    fun setData(data: List<PhotocardDto>, updated: Int) {
        adapter.updateData(data.take(updated))
        if (scrollPosition != 0) {
            photocardList.scrollToPosition(scrollPosition)
            scrollPosition = 0
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
        loginDialog.subscribe()
        registrationDialog.subscribe()
    }

    override fun onViewDestroyed(removedByFlow: Boolean) {
        super.onViewDestroyed(removedByFlow)
        saveHierarchyState(state)
        loginDialog.unsubscribe()
        registrationDialog.unsubscribe()
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable("SUPER", super.onSaveInstanceState())
        val lm = photocardList.layoutManager as GridLayoutManager
        bundle.putInt("SCROLL", lm.findFirstVisibleItemPosition())
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val bundle = state as Bundle
        super.onRestoreInstanceState(bundle.getParcelable("SUPER"))
        scrollPosition = bundle.getInt("SCROLL")
    }

    fun showFilterWarning() {
        val snackbar = Snackbar.make(this, R.string.main_message_filter, Snackbar.LENGTH_LONG)
                .setAction(R.string.main_message_filter_action, { presenter.resetFilter() })
        snackbar.view.setBackgroundColor(ContextCompat.getColor(context, R.color.text_color))
        snackbar.show()
    }
}

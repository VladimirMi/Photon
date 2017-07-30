package io.github.vladimirmi.photon.features.newcard

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import com.jakewharton.rxbinding2.support.v4.view.pageSelections
import com.transitionseverywhere.TransitionManager
import flow.Direction
import flow.Flow
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.flow.FlowLifecycles
import io.github.vladimirmi.photon.utils.prepareChangeScreenTransitionSet
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.view_choose.view.*
import kotlinx.android.synthetic.main.view_newcard.view.*

/**
 * Created by Vladimir Mikhalev 19.06.2017.
 */

class NewCardView(context: Context, attrs: AttributeSet)
    : BaseView<NewCardPresenter, NewCardView>(context, attrs),
        FlowLifecycles.ActivityResultListener, FlowLifecycles.PermissionRequestListener {

    private lateinit var disposable: Disposable
    private val state = Flow.getKey<NewCardScreen>(context)!!.state
    private val pagerAdapter by lazy {
        if (Flow.getKey<NewCardScreen>(context)?.album == null) {
            NewCardPagerAdapter(3)
        } else {
            NewCardPagerAdapter(2)
        }
    }

    override fun initDagger(context: Context) {
        DaggerService.getComponent<NewCardScreen.Component>(context).inject(this)
    }

    override fun onBackPressed() = presenter.onBackPressed()

    override fun initView() {
        choose_btn.setOnClickListener { presenter.choosePhoto() }
        save.setOnClickListener { presenter.savePhotocard() }
        cancel.setOnClickListener { presenter.clearPhotocard() }
        view_pager.adapter = pagerAdapter
        ic_action_back.setOnClickListener {
            changePage(Page.fromIndex(view_pager.currentItem - 1))
        }
        ic_action_forward.setOnClickListener {
            changePage(Page.fromIndex(view_pager.currentItem + 1))
        }
        disposable = view_pager.pageSelections().skipInitialValue()
                .subscribe { changePage(Page.fromIndex(it)) }
    }

    override fun onViewDestroyed(removedByFlow: Boolean) {
        super.onViewDestroyed(removedByFlow)
        disposable.dispose()
        view_pager.adapter = null
        if (removedByFlow) state.clear()
    }

    fun clearView() {
        changePage(Page.INFO, smoothScroll = false)
        pagerAdapter.notifyDataSetChanged()
    }

    fun showPhotoParams() {
        prepareTransition(choose_view, newcard_view, Direction.FORWARD)
        choose_view.visibility = View.GONE
        newcard_view.visibility = View.VISIBLE
    }

    fun showPhotoChoose() {
        prepareTransition(newcard_view, choose_view, Direction.BACKWARD)
        choose_view.visibility = View.VISIBLE
        newcard_view.visibility = View.GONE
    }

    fun changePage(page: Page, smoothScroll: Boolean = true) {
        pager_header.text = context.getString(R.string.newcard_pager_header,
                page.index + 1,
                pagerAdapter.count)
        presenter.saveCurrentPage(page)
        if (page.index != view_pager.currentItem) {
            view_pager.setCurrentItem(page.index, smoothScroll)
        }
        when (page) {
            Page.INFO -> ic_action_back.isEnabled = false
            Page.PARAMS -> {
                ic_action_back.isEnabled = true
                ic_action_forward.isEnabled = true
            }
            Page.ALBUMS -> ic_action_forward.isEnabled = false
        }
    }

    private fun prepareTransition(previousView: View,
                                  newView: View,
                                  direction: Direction) {
        val set = prepareChangeScreenTransitionSet(previousView, newView, direction)
        TransitionManager.beginDelayedTransition(this, set)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        presenter.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        presenter.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}


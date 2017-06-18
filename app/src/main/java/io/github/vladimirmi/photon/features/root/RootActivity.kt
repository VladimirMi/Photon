package io.github.vladimirmi.photon.features.root

import android.app.ProgressDialog
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.view.menu.MenuBuilder
import android.support.v7.view.menu.MenuPopupHelper
import android.support.v7.widget.PopupMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import flow.Flow
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseScreen
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.features.splash.SplashScreen
import io.github.vladimirmi.photon.flow.BottomNavDispatcher
import io.github.vladimirmi.photon.flow.FlowActivity
import kotlinx.android.synthetic.main.activity_root.*
import kotlinx.android.synthetic.main.view_menu_item.view.*
import javax.inject.Inject

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

class RootActivity : FlowActivity(), IRootView {

    @Inject internal lateinit var presenter: RootPresenter


    //region =============== Life cycle ==============

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)
        setupFlowDispatcher(root_container, view_container)

        initToolbar()
        initDagger()
        presenter.takeView(this)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        bottom_menu.setOnNavigationItemSelectedListener(BottomNavDispatcher(Flow.get(this)))
    }

    override fun onStart() {
        if (!presenter.hasActiveView()) presenter.takeView(this)
        super.onStart()
    }

    override fun onStop() {
        presenter.dropView(this)
        super.onStop()
    }

    //endregion

    private fun initToolbar() {
        setSupportActionBar(toolbar)
    }

    private fun initDagger() {
        DaggerService.rootActivityComponent.inject(this)
    }

    override val defaultKey: BaseScreen<*>
        get() = SplashScreen()


    //region =============== IRootView ==============

    private var progressDialog: ProgressDialog? = null

    override fun showLoading() {
        hideLoading()
        progressDialog = ProgressDialog(this).apply {
            setMessage(this@RootActivity.getString(R.string.message_wait))
            isIndeterminate = true
            setCancelable(false)
            show()
        }
    }

    override fun hideLoading() {
        if (progressDialog != null) {
            progressDialog?.apply { hide(); dismiss() }
            progressDialog = null
        }
    }

    override fun showMessage(@StringRes stringId: Int) {
        showMessage(getString(stringId))
    }

    override fun showMessage(string: String) {
        Snackbar.make(root_container, string, Snackbar.LENGTH_LONG).show()
    }

    //endregion

    //region =============== IViewBuilder ==============

    override fun setBottomMenuChecked(index: Int) {
        //todo implement me
    }

    override fun setBottomMenuVisible(visible: Boolean) {
        if (visible) {
            bottom_menu.visibility = View.VISIBLE
        } else {
            bottom_menu.visibility = View.GONE
        }
    }

    override fun setToolbarVisible(visible: Boolean) {
        if (visible) {
            toolbar.visibility = View.VISIBLE
        } else {
            toolbar.visibility = View.GONE
        }
    }

    override fun setToolbarTitle(@StringRes titleId: Int) {
        supportActionBar?.setTitle(titleId)
    }

    override fun enableTabs(enabled: Boolean) {
        removeTabLayout()
        if (enabled) setTabLayout()
    }

    private fun setTabLayout() {
        val tabs = TabLayout(this)
        val pager = view_container.findViewWithTag("pager") as ViewPager
        tabs.setupWithViewPager(pager)
        appbar.addView(tabs)
        pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
    }

    private fun removeTabLayout() {
        val tabs = appbar.getChildAt(1)
        if (tabs != null && tabs is TabLayout) {
            appbar.removeView(tabs)
        }
    }

    private var actionBarMenuItems: List<MenuItemHolder> = ArrayList()

    override fun setMenuItems(menuItems: List<MenuItemHolder>) {
        actionBarMenuItems = menuItems
        supportInvalidateOptionsMenu()
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if (!actionBarMenuItems.isEmpty()) {
            for (menuItemHolder in actionBarMenuItems) {
                val item = menu.add(menuItemHolder.itemTitle)
                if (menuItemHolder.hasPopupMenu()) {
                    var actionView: View
                    if (menuItemHolder.actionView == null) {
                        actionView = LayoutInflater.from(this).inflate(R.layout.view_menu_item, toolbar, false)
                    } else {
                        actionView = menuItemHolder.actionView
                    }
                    actionView.icon.setImageDrawable(ContextCompat.getDrawable(this, menuItemHolder.iconResId as Int))
                    item.actionView = actionView

                    val popup = PopupMenu(this, actionView)
                    popup.inflate(menuItemHolder.popupMenu!!)
                    popup.setOnMenuItemClickListener {
                        kotlin.run {
                            menuItemHolder.actions(it)
                            return@run true
                        }
                    }
                    val menuHelper = MenuPopupHelper(this, popup.menu as MenuBuilder,
                            toolbar)
                    if (popup.menu.getItem(0).icon != null) {
                        menuHelper.setForceShowIcon(true)
                    }
                    menuHelper.setAnchorView(actionView)
                    actionView.setOnClickListener { menuHelper.show(0, -actionView.height) }
                } else {
                    item.setIcon(menuItemHolder.iconResId!!)
                    item.setOnMenuItemClickListener {
                        kotlin.run {
                            menuItemHolder.actions(it)
                            return@run true
                        }
                    }
                }
                item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
            }
        } else {
            menu.clear()
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun setBackground(backgroundId: Int) {
        root_container.setBackgroundResource(backgroundId)
    }

    //endregion
}

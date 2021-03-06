package io.github.vladimirmi.photon.presentation.root

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.view.menu.MenuBuilder
import android.support.v7.view.menu.MenuPopupHelper
import android.support.v7.widget.AppCompatDrawableManager
import android.support.v7.widget.PopupMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import flow.Flow
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseScreen
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.flow.BottomNavigationHistory
import io.github.vladimirmi.photon.flow.FlowActivity
import io.github.vladimirmi.photon.presentation.splash.SplashScreen
import io.github.vladimirmi.photon.utils.Constants
import kotlinx.android.synthetic.main.activity_root.*
import kotlinx.android.synthetic.main.view_menu_item.view.*
import javax.inject.Inject

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

class RootActivity : FlowActivity(), IRootView {

    @Inject internal lateinit var presenter: RootPresenter
    private val popups = ArrayList<MenuPopupHelper>()
    private val toolBarMenuItems = ArrayList<MenuItemHolder>()

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
        val history = presenter.bottomHistory
        history.init(Flow.get(this))
        navigateTo(history.currentItem)
        bottom_menu.setOnNavigationItemSelectedListener(history)
    }

    override fun onStart() {
        if (!presenter.hasActiveView()) presenter.takeView(this)
        super.onStart()
    }

    override fun onStop() {
        popups.forEach { it.dismiss() }
        super.onStop()
        presenter.dropView(this)
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

    override fun showLoading() {
        loading.smoothToShow()
        loading.show()
    }

    override fun hideLoading() {
        loading.hide()
    }

    override fun showPermissionSnackBar() {
        Snackbar.make(root_container, R.string.message_permission_need, Snackbar.LENGTH_LONG)
                .setAction(R.string.message_permission_need_action, { openApplicationSettings() })
                .show()
    }

    private fun openApplicationSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + packageName))
        startActivityForResult(intent, Constants.REQUEST_SETTINGS_INTENT)
    }

    override fun navigateTo(bottomItem: BottomNavigationHistory.BottomItem) {
        bottom_menu.selectedItemId = bottomItem.id
    }

    //endregion

    //region =============== IToolbarBuilder ==============

    override fun clearToolbar() {
        toolBarMenuItems.clear()
        popups.clear()
        toolbar.menu.clear()
    }

    override fun setBottomMenuVisible(visible: Boolean) {
        if (visible) {
            bottom_menu.visibility = View.VISIBLE
            bottom_menu_shadow.visibility = View.VISIBLE
        } else {
            bottom_menu.visibility = View.GONE
            bottom_menu_shadow.visibility = View.GONE
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

    override fun enableBackNavigation(backNavEnabled: Boolean) {
        supportActionBar?.setDisplayHomeAsUpEnabled(backNavEnabled)
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

    override fun setMenuItems(menuItems: List<MenuItemHolder>) {
        toolBarMenuItems.clear()
        toolBarMenuItems.addAll(menuItems)
        invalidateOptionsMenu()
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if (toolBarMenuItems.isNotEmpty()) {
            for (menuItemHolder in toolBarMenuItems) {
                val item = menu.add(menuItemHolder.itemTitle)
                if (menuItemHolder.hasPopupMenu()) {
                    configurePopupFor(item, menuItemHolder)
                } else {
                    item.setIcon(menuItemHolder.iconResId)
                    item.setOnMenuItemClickListener {
                        menuItemHolder.actions(it)
                        true
                    }
                }
                item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    private fun configurePopupFor(item: MenuItem, menuItemHolder: MenuItemHolder) {
        val actionView = LayoutInflater.from(this).inflate(R.layout.view_menu_item, toolbar, false)
        actionView.icon.setImageDrawable(AppCompatDrawableManager.get().getDrawable(this, menuItemHolder.iconResId))
        item.actionView = actionView

        val popup = PopupMenu(this, actionView)
        popup.inflate(menuItemHolder.popupMenu!!)
        popup.setOnMenuItemClickListener {
            menuItemHolder.actions(it)
            true
        }
        val menuHelper = MenuPopupHelper(this, popup.menu as MenuBuilder, actionView)
        (0 until popup.menu.size())
                .filter { popup.menu.getItem(it).icon != null }
                .forEach { menuHelper.setForceShowIcon(true); return@forEach }
        actionView.setOnClickListener {
            if (!menuHelper.isShowing) menuHelper.show(0, -actionView.height)
        }
        popups.add(menuHelper)
    }

    override fun setBackground(backgroundId: Int) {
        root_container.setBackgroundResource(backgroundId)
    }

    //endregion
}

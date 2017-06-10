package io.github.vladimirmi.photon.features.root

import android.app.ProgressDialog
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.view.MenuItemCompat
import android.support.v4.view.ViewPager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseScreen
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.features.splash.SplashScreen
import io.github.vladimirmi.photon.flow.FlowActivity
import kotlinx.android.synthetic.main.activity_root.*
import timber.log.Timber
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
        setupFlowDispatcher(coordinator_container, view_container)

        initToolbar()
        initDagger()
        presenter.takeView(this)
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
        Snackbar.make(coordinator_container, string, Snackbar.LENGTH_LONG).show()
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
        Timber.e("prepare")
        if (!actionBarMenuItems.isEmpty()) {
            for (menuItemHolder in actionBarMenuItems) {
                val item = menu.add(menuItemHolder.itemTitle)
                item.setIcon(menuItemHolder.iconResId)
                MenuItemCompat.setActionProvider(item, menuItemHolder.actionProvider)
                item.setOnMenuItemClickListener(menuItemHolder.listener)
                item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
            }
        } else {
            menu.clear()
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun setBackground(backgroundId: Int) {
        coordinator_container.setBackgroundResource(backgroundId)
    }

    //endregion
}

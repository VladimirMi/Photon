package io.github.vladimirmi.photon.features.root

import android.content.Context
import android.support.annotation.StringRes
import android.view.MenuItem
import android.view.View
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.di.DaggerScope
import mortar.Presenter
import mortar.bundler.BundleService
import java.util.*

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

@DaggerScope(RootActivity::class)
class RootPresenter(val model: IRootModel) :
        Presenter<IRootView>() {

    override fun extractBundleService(view: IRootView?): BundleService {
        return BundleService.getBundleService(view as Context)
    }

    fun hasActiveView() = hasView()

    fun getNewToolbarBuilder(): ToolbarBuilder = ToolbarBuilder(view)

    fun isUserAuth(): Boolean {
        return model.isUserAuth()
    }

    fun showMessage(msg: String) {
        view.showMessage(msg)
    }

    fun showMessage(id: Int) {
        view.showMessage(id)
    }

    fun showLoading() {
        view.showLoading()
    }

    fun hideLoading() {
        view.hideLoading()
    }
}

class ToolbarBuilder(val rootView: IRootView) {
    private var isToolbarVisible = true
    @StringRes private var toolbarTitleId = R.string.app_name
    private var bottomMenuEnabled = true
    private var bottomItemIndex = 0
    private var isTabsEnabled = false
    private var backgroundId = R.color.background
    private val menuItems = ArrayList<MenuItemHolder>()

    fun setToolbarVisible(toolbarVisible: Boolean): ToolbarBuilder {
        isToolbarVisible = toolbarVisible
        return this
    }

    fun setBottomMenuEnabled(menuEnabled: Boolean): ToolbarBuilder {
        bottomMenuEnabled = menuEnabled
        return this
    }

    fun setToolbarTitleId(@StringRes toolbarTitleId: Int): ToolbarBuilder {
        this.toolbarTitleId = toolbarTitleId
        return this
    }

    fun setTabsEnabled(tabsEnabled: Boolean): ToolbarBuilder {
        isTabsEnabled = tabsEnabled
        return this
    }

    fun setBackGround(resId: Int): ToolbarBuilder {
        backgroundId = resId
        return this
    }

    fun addAction(menuItemHolder: MenuItemHolder): ToolbarBuilder {
        menuItems.add(menuItemHolder)
        return this
    }

    fun build() {
        rootView.setBottomMenuVisible(bottomMenuEnabled)
        rootView.setToolbarVisible(isToolbarVisible)
        rootView.setToolbarTitle(toolbarTitleId)
        rootView.setBottomMenuChecked(bottomItemIndex)
        rootView.enableTabs(isTabsEnabled)
        rootView.setBackground(backgroundId)
        rootView.setMenuItems(menuItems)
    }
}

class MenuItemHolder(val itemTitle: String = "",
                     val iconResId: Int? = null,
                     val actionView: View? = null,
                     val popupMenu: Int? = null,
                     val actions: (MenuItem) -> Unit) {
    fun hasPopupMenu() = popupMenu != null
}



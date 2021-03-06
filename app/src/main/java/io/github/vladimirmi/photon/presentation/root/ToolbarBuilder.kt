package io.github.vladimirmi.photon.presentation.root

import android.support.annotation.StringRes
import android.view.MenuItem
import io.github.vladimirmi.photon.R
import java.util.*

class ToolbarBuilder(private val rootView: IRootView) {
    private var isToolbarVisible = true
    @StringRes private var toolbarTitleId = R.string.app_name
    private var backNavEnabled = false
    private var bottomMenuEnabled = true
    private var isTabsEnabled = false
    private var backgroundId = R.color.background
    private val menuItems = ArrayList<MenuItemHolder>()

    fun setToolbarVisible(toolbarVisible: Boolean): ToolbarBuilder {
        isToolbarVisible = toolbarVisible
        return this
    }

    fun setToolbarTitleId(@StringRes titleId: Int): ToolbarBuilder {
        toolbarTitleId = titleId
        return this
    }

    fun setBackNavigationEnabled(backEnabled: Boolean): ToolbarBuilder {
        backNavEnabled = backEnabled
        return this
    }

    fun setBottomMenuEnabled(menuEnabled: Boolean): ToolbarBuilder {
        bottomMenuEnabled = menuEnabled
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
        rootView.setToolbarVisible(isToolbarVisible)
        rootView.setToolbarTitle(toolbarTitleId)
        rootView.enableBackNavigation(backNavEnabled)
        rootView.setBottomMenuVisible(bottomMenuEnabled)
        rootView.enableTabs(isTabsEnabled)
        rootView.setBackground(backgroundId)
        rootView.setMenuItems(menuItems)
    }
}

class MenuItemHolder(val itemTitle: String,
                     val iconResId: Int,
                     val actions: (MenuItem) -> Unit,
                     val popupMenu: Int? = null) {
    //todo action to weakref
    fun hasPopupMenu() = popupMenu != null
}
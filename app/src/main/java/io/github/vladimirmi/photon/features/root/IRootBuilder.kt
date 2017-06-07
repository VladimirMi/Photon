package io.github.vladimirmi.photon.features.root

import android.support.annotation.StringRes

interface IRootBuilder {

    fun setToolbarVisible(visible: Boolean)

    fun setToolbarTitle(@StringRes titleId: Int)

    fun setBottomMenuChecked(bottomItemIndex: Int)

    fun enableTabs(tabsEnabled: Boolean)

    fun setBackground(backgroundId: Int)

    fun setMenuItems(menuItems: List<MenuItemHolder>)

}
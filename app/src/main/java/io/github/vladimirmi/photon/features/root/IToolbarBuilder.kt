package io.github.vladimirmi.photon.features.root

import android.support.annotation.StringRes

interface IToolbarBuilder {

    fun clearToolbar()

    fun setToolbarVisible(visible: Boolean)

    fun setToolbarTitle(@StringRes titleId: Int)

    fun setBottomMenuVisible(visible: Boolean)

    fun enableTabs(enabled: Boolean)

    fun setBackground(backgroundId: Int)

    fun setMenuItems(menuItems: List<MenuItemHolder>)

    fun enableBackNavigation(backNavEnabled: Boolean)

}
package io.github.vladimirmi.photon.features.root

import android.content.Context
import android.support.annotation.StringRes
import android.support.v4.view.ActionProvider
import android.view.MenuItem
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

    fun getNewRootBuilder(): RootBuilder = RootBuilder()

    inner class RootBuilder {
        private var isToolbarVisible = true
        @StringRes private var toolbarTitleId = R.string.app_name
        private var bottomMenuEnabled = true
        private var bottomItemIndex = 0
        private var isTabsEnabled = false
        private var backgroundId = R.color.background
        private val menuItems = ArrayList<MenuItemHolder>()

        fun setToolbarVisible(toolbarVisible: Boolean): RootBuilder {
            isToolbarVisible = toolbarVisible
            return this
        }

        fun setBottomMenuEnabled(menuEnabled: Boolean): RootBuilder {
            bottomMenuEnabled = menuEnabled
            return this
        }

        fun setToolbarTitleId(@StringRes toolbarTitleId: Int): RootBuilder {
            this.toolbarTitleId = toolbarTitleId
            return this
        }

        fun setTabsEnabled(tabsEnabled: Boolean): RootBuilder {
            isTabsEnabled = tabsEnabled
            return this
        }

        fun setBackGround(resId: Int): RootBuilder {
            backgroundId = resId
            return this
        }

        fun addAction(menuItemHolder: MenuItemHolder): RootBuilder {
            menuItems.add(menuItemHolder)
            return this
        }

        fun build() {
            view.setBottomMenuVisible(bottomMenuEnabled)
            view.setToolbarVisible(isToolbarVisible)
            view.setToolbarTitle(toolbarTitleId)
            view.setBottomMenuChecked(bottomItemIndex)
            view.enableTabs(isTabsEnabled)
            view.setBackground(backgroundId)
            view.setMenuItems(menuItems)
        }
    }
}

class MenuItemHolder(val itemTitle: String = "",
                     val iconResId: Int,
                     val listener: MenuItem.OnMenuItemClickListener? = null,
                     val actionProvider: ActionProvider? = null)



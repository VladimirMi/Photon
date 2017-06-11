package io.github.vladimirmi.photon.features.main

import android.content.Context
import android.support.v4.view.ActionProvider
import android.view.SubMenu
import io.github.vladimirmi.photon.R


/**
 * Created by Vladimir Mikhalev 10.06.2017.
 */

class LoginActionProvider(context: Context,
                          val isLogin: Boolean = false,
                          val loginAction: (() -> Boolean)? = null,
                          val logoutAction: (() -> Boolean)? = null,
                          val registrationAction: (() -> Boolean)? = null)
    : ActionProvider(context) {

    override fun onCreateActionView() = null

    override fun hasSubMenu() = true

    override fun onPrepareSubMenu(subMenu: SubMenu) {
        subMenu.clear()
        if (isLogin) {
            subMenu.add(context.getString(R.string.menu_search_exit))
                    .setOnMenuItemClickListener { run(logoutAction!!) }
        } else {
            subMenu.add(context.getString(R.string.menu_search_enter))
                    .setOnMenuItemClickListener { run(loginAction!!) }
            subMenu.add(context.getString(R.string.menu_search_registration))
                    .setOnMenuItemClickListener { run(registrationAction!!) }
        }
    }
}
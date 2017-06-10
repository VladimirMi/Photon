package io.github.vladimirmi.photon.utils

import android.content.Context
import android.support.v4.view.ActionProvider
import android.view.SubMenu


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
            subMenu.add("Выйти").setOnMenuItemClickListener { run(logoutAction!!) }
        } else {
            subMenu.add("Войти").setOnMenuItemClickListener { run(loginAction!!) }
            subMenu.add("Зарегистрироваться").setOnMenuItemClickListener { run(registrationAction!!) }
        }
    }
}
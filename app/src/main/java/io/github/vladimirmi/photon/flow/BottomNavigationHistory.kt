package io.github.vladimirmi.photon.flow

import android.support.design.widget.BottomNavigationView
import android.view.MenuItem
import flow.Direction
import flow.Flow
import flow.History
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseScreen
import io.github.vladimirmi.photon.flow.BottomNavigationHistory.BottomItem.*
import io.github.vladimirmi.photon.presentation.auth.AuthScreen
import io.github.vladimirmi.photon.presentation.newcard.NewCardScreen
import io.github.vladimirmi.photon.presentation.profile.ProfileScreen
import io.github.vladimirmi.photon.presentation.splash.SplashScreen

/**
 * Created by Vladimir Mikhalev 15.06.2017.
 */

class BottomNavigationHistory(var authMode: Boolean)
    : BottomNavigationView.OnNavigationItemSelectedListener {

    enum class BottomItem(val id: Int) {
        MAIN(R.id.nav_bottom_main),
        PROFILE(R.id.nav_bottom_profile),
        LOAD(R.id.nav_bottom_load);

        companion object {
            private val map = BottomItem.values().associateBy(BottomItem::id)
            fun fromId(id: Int) = map[id]
        }
    }

    val historyMap = hashMapOf(MAIN to History.single(SplashScreen()),
            PROFILE to History.single(ProfileScreen()),
            LOAD to History.single(NewCardScreen()))

    lateinit var flow: Flow
    var currentItem = MAIN
        private set

    fun init(flow: Flow) {
        this.flow = flow
        restoreCurrentItem()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        dispatch(from = currentItem, to = Companion.fromId(item.itemId)!!)
        return true
    }

    private fun dispatch(from: BottomItem, to: BottomItem, saveHistory: Boolean = true) {
        currentItem = to
        val direction = getDirection(from, to)
        if (saveHistory) historyMap[from] = flow.history
        handleAuthMode(to)

        flow.setHistory(historyMap[to]!!, direction)
    }

    private fun handleAuthMode(to: BottomItem) {
        if (authMode) {
            if (to == PROFILE || to == LOAD) historyMap[to] = History.single(AuthScreen())

        } else {
            val top = historyMap[to]!!.top<BaseScreen<*>>()
            if (top is AuthScreen) {
                if (to == PROFILE) historyMap[to] = History.single(ProfileScreen())
                if (to == LOAD) historyMap[to] = History.single(NewCardScreen())
            }
        }
    }

    private fun getDirection(from: BottomItem, to: BottomItem): Direction =
            when (from) {
                MAIN -> Direction.FORWARD
                PROFILE -> if (to == MAIN) Direction.BACKWARD else Direction.FORWARD
                LOAD -> Direction.BACKWARD
            }

    private fun restoreCurrentItem() {
        dispatch(BottomNavigationHistory.BottomItem.MAIN, currentItem, saveHistory = false)
    }
}
package io.github.vladimirmi.photon.flow

import android.support.design.widget.BottomNavigationView
import android.view.MenuItem
import flow.Direction
import flow.Flow
import flow.History
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseScreen
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.features.auth.AuthScreen
import io.github.vladimirmi.photon.features.main.MainScreen
import io.github.vladimirmi.photon.features.newcard.NewCardScreen
import io.github.vladimirmi.photon.features.profile.ProfileScreen
import io.github.vladimirmi.photon.flow.BottomNavDispatcher.BottomItem.*

/**
 * Created by Vladimir Mikhalev 15.06.2017.
 */

class BottomNavDispatcher(private val flowInstance: Flow) : BottomNavigationView.OnNavigationItemSelectedListener {

    enum class BottomItem(val id: Int) {
        MAIN(R.id.nav_bottom_main),
        PROFILE(R.id.nav_bottom_profile),
        LOAD(R.id.nav_bottom_load);

        companion object {
            private val map = BottomItem.values().associateBy(BottomItem::id)
            fun fromId(id: Int) = map[id]
        }
    }

    var currentItem = MAIN

    private val dm = DaggerService.appComponent.dataManager()
    val historyMap = hashMapOf(MAIN to History.single(MainScreen()),
            PROFILE to History.single(ProfileScreen()),
            LOAD to History.single(NewCardScreen()))


    fun dispatch(from: BottomItem, to: BottomItem) {
        val direction = getDirection(from, to)
        historyMap[from] = flowInstance.history

        if (dm.isUserAuth()) {
            val top = historyMap[to]!!.top<BaseScreen<*>>()
            if (top is AuthScreen) {
                if (to == PROFILE) historyMap[to] = History.single(ProfileScreen())
                if (to == LOAD) historyMap[to] = History.single(NewCardScreen())
            }
        } else {
            if (to == PROFILE || to == LOAD) historyMap[to] = History.single(AuthScreen())
        }

        flowInstance.setHistory(historyMap[to]!!, direction)
        currentItem = to
    }

    private fun getDirection(from: BottomItem, to: BottomItem): Direction {
        return when (from) {
            MAIN -> Direction.FORWARD
            PROFILE -> if (to == MAIN) Direction.BACKWARD else Direction.FORWARD
            LOAD -> Direction.BACKWARD
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        dispatch(from = currentItem, to = Companion.fromId(item.itemId)!!)
        return true
    }
}
package io.github.vladimirmi.photon.features.root

import io.github.vladimirmi.photon.core.IView
import io.github.vladimirmi.photon.flow.BottomNavHistory


/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

interface IRootView : IView, IToolbarBuilder {

    fun showLoading()
    fun hideLoading()
    fun showPermissionSnackBar()
    fun navigateTo(bottomItem: BottomNavHistory.BottomItem)
}


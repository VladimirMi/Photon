package io.github.vladimirmi.photon.features.root

import android.support.annotation.StringRes
import io.github.vladimirmi.photon.core.IView


/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

interface IRootView : IView, IRootBuilder {

    fun showLoading()

    fun hideLoading()

    fun showMessage(@StringRes stringId: Int)

    fun showMessage(string: String)

}


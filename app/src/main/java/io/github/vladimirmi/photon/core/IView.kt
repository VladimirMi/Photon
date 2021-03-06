package io.github.vladimirmi.photon.core

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

interface IView {
    fun showMessage(string: String)
    fun showMessage(stringId: Int)
    fun showError(stringId: Int, vararg formatArgs: Any = emptyArray())
    fun showNetError()
    fun showAuthError()
}

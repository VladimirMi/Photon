package io.github.vladimirmi.photon.core

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

interface IPresenter<in V : IView> {

    fun takeView(v: V)

    fun dropView()
}


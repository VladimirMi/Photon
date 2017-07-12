package io.github.vladimirmi.photon.core

import flow.ClassKey

/**
 * Developer Vladimir Mikhalev, 30.05.2017
 */

abstract class BaseScreen<in T> : ClassKey() {

    open val scopeName: String = javaClass.name

    abstract fun createScreenComponent(parentComponent: T): Any

    abstract val layoutResId: Int
}

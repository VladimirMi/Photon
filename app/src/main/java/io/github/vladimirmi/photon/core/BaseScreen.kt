package io.github.vladimirmi.photon.core

import android.support.annotation.LayoutRes
import flow.ClassKey

/**
 * Developer Vladimir Mikhalev, 30.05.2017
 */

abstract class BaseScreen<in T> : ClassKey() {

    val scopeName: String
        get() = javaClass.name

    abstract fun createScreenComponent(parentComponent: T): Any

    @get:LayoutRes
    abstract val layoutResId: Int
}

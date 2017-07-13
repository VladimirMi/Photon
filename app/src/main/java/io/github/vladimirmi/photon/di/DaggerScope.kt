package io.github.vladimirmi.photon.di

import javax.inject.Scope
import kotlin.reflect.KClass

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

@Scope
@Retention(AnnotationRetention.SOURCE)
annotation class DaggerScope(val value: KClass<*>)

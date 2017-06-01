package io.github.vladimirmi.photon.di

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

import javax.inject.Scope
import kotlin.reflect.KClass

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

@Scope
@Retention(RetentionPolicy.SOURCE)
annotation class DaggerScope(val value: KClass<*>)

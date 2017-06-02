package io.github.vladimirmi.photon.core

import android.app.Application

import io.github.vladimirmi.photon.BuildConfig
import io.github.vladimirmi.photon.di.DaggerService
import timber.log.Timber

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

class App : Application() {

    private var appInit = false

    override fun onCreate() {
        super.onCreate()
        DaggerService.createAppComponent(applicationContext)

        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
        appInit = true
    }

    override fun getSystemService(name: String): Any {
        if (appInit && DaggerService.rootScope.hasService(name)) {
            return DaggerService.rootScope.getService(name)
        } else {
            return super.getSystemService(name)
        }
    }
}

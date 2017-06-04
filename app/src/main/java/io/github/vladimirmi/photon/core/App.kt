package io.github.vladimirmi.photon.core

import android.app.Application
import io.github.vladimirmi.photon.BuildConfig
import io.github.vladimirmi.photon.di.DaggerService
import io.realm.Realm
import timber.log.Timber

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        DaggerService.createAppComponent(applicationContext)

        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}

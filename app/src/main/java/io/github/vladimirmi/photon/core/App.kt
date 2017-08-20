package io.github.vladimirmi.photon.core

import android.app.Application
import android.content.Context
import com.crashlytics.android.Crashlytics
import com.squareup.leakcanary.RefWatcher
import io.fabric.sdk.android.Fabric
import io.github.vladimirmi.photon.BuildConfig
import io.github.vladimirmi.photon.di.DaggerService
import io.realm.Realm
import timber.log.Timber

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

class App : Application() {

    private lateinit var refWatcher: RefWatcher

    companion object {
        fun getRefWatcher(context: Context): RefWatcher {
            val application = context.applicationContext as App
            return application.refWatcher
        }
    }

    override fun onCreate() {
        super.onCreate()
//        if (LeakCanary.isInAnalyzerProcess(this)) return
//        refWatcher = LeakCanary.install(this)

        Realm.init(this)

        DaggerService.createAppComponent(applicationContext)

        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())

        Fabric.with(this, Crashlytics())
    }
}

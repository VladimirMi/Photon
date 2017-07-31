package io.github.vladimirmi.photon.di

import android.content.Context
import dagger.Component
import io.github.vladimirmi.photon.core.App
import io.github.vladimirmi.photon.data.managers.Cache
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.di.modules.LocaleModule
import io.github.vladimirmi.photon.di.modules.NetworkModule
import io.github.vladimirmi.photon.features.root.RootActivityComponent

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */


@DaggerScope(App::class)
@Component(modules = arrayOf(NetworkModule::class, LocaleModule::class))
interface AppComponent {
    fun rootActivityComponentBuilder(): RootActivityComponent.Builder
    fun dataManager(): DataManager
    fun context(): Context
    //    fun watcher(): RefWatcher
    fun cache(): Cache
}

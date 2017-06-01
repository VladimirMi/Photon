package io.github.vladimirmi.photon.di

import android.content.Context

import flow.Flow
import io.github.vladimirmi.photon.core.BaseScreen
import io.github.vladimirmi.photon.di.modules.LocaleModule
import io.github.vladimirmi.photon.di.modules.NetworkModule
import io.github.vladimirmi.photon.features.root.RootActivityComponent
import io.github.vladimirmi.photon.features.root.RootActivityModule

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

object DaggerService {

    lateinit var appComponent: AppComponent
        private set
    val rootActivityComponent: RootActivityComponent by lazy {
        createRootActivityComponent()
    }

    fun createAppComponent(context: Context) {
        appComponent = DaggerAppComponent.builder()
                .localeModule(LocaleModule(context))
                .networkModule(NetworkModule())
                .build()
    }

    private fun createRootActivityComponent(): RootActivityComponent {
        return appComponent.rootActivityComponentBuilder()
                .module(RootActivityModule())
                .build()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getComponent(context: Context): T {
        val screen = Flow.getKey<BaseScreen<*>>(context)
        if (screen != null) {
            return Flow.getService<Any>(screen.scopeName, context) as T
        }
        return appComponent as T
    }
}

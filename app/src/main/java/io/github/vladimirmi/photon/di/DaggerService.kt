package io.github.vladimirmi.photon.di

import android.content.Context
import io.github.vladimirmi.photon.di.modules.LocaleModule
import io.github.vladimirmi.photon.di.modules.NetworkModule
import io.github.vladimirmi.photon.features.root.RootActivity
import io.github.vladimirmi.photon.features.root.RootActivityComponent
import io.github.vladimirmi.photon.features.root.RootActivityModule
import io.realm.Realm
import mortar.MortarScope
import mortar.bundler.BundleServiceRunner

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

object DaggerService {

    const val SERVICE_NAME: String = "DAGGER_SERVICE"

    lateinit var appComponent: AppComponent
        private set

    val rootActivityComponent: RootActivityComponent by lazy {
        createRootActivityComponent()
    }

    fun createAppComponent(context: Context, realm: Realm) {
        appComponent = DaggerAppComponent.builder()
                .localeModule(LocaleModule(context, realm))
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
        return context.getSystemService(SERVICE_NAME) as T
    }

    val rootScope: MortarScope by lazy {
        createRootScope()
    }

    val rootActivityScope: MortarScope by lazy {
        createRootActivityScope()
    }

    private fun createRootScope(): MortarScope {
        return MortarScope.buildRootScope()
                .withService(SERVICE_NAME, appComponent)
                .build("Root")
    }

    private fun createRootActivityScope(): MortarScope {
        return rootScope.buildChild()
                .withService(SERVICE_NAME, rootActivityComponent)
                .withService(BundleServiceRunner.SERVICE_NAME, BundleServiceRunner())
                .build(RootActivity::class.java.name)
    }
}

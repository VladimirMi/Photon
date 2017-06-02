package io.github.vladimirmi.photon.flow

import flow.Services
import flow.ServicesFactory
import flow.TreeKey
import io.github.vladimirmi.photon.core.BaseScreen
import io.github.vladimirmi.photon.di.DaggerService
import mortar.MortarScope
import timber.log.Timber

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

internal class FlowServiceFactory : ServicesFactory() {
    override fun bindServices(services: Services.Binder) {
        val screen: BaseScreen<Any?>
        if (services.getKey<Any>() is BaseScreen<*>) {
            screen = services.getKey()
        } else {
            throw IllegalStateException("${services.getKey<Any>().javaClass.name} isn't BaseScreen")
        }

        val screenComponent: Any
        var parentScope: MortarScope = DaggerService.rootActivityScope //default parent scope
        if (screen is TreeKey) {
            val parentScreen = screen.parentKey as BaseScreen<*>
            val parentScopeName = parentScreen.scopeName
            parentScope = services.getService<MortarScope>(parentScopeName)!!
            screenComponent = screen.createScreenComponent(parentScope.getService(DaggerService.SERVICE_NAME))
        } else {
            screenComponent = screen.createScreenComponent(parentScope.getService(DaggerService.SERVICE_NAME))
        }
        val newScope = parentScope.buildChild()
                .withService(DaggerService.SERVICE_NAME, screenComponent)
                .build(screen.scopeName)
        Timber.d("Build new scope with name ${newScope.name}")

        services.bind(newScope.name, newScope)
    }

    override fun tearDownServices(services: Services) {
        services.getService<MortarScope>(services.getKey<Any>().javaClass.name)?.destroy()
        super.tearDownServices(services)
    }
}

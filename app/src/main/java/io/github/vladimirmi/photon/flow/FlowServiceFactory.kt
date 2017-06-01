package io.github.vladimirmi.photon.flow

import flow.Services
import flow.ServicesFactory
import flow.TreeKey
import io.github.vladimirmi.photon.core.BaseScreen
import io.github.vladimirmi.photon.di.DaggerService

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

internal class FlowServiceFactory : ServicesFactory() {
    override fun bindServices(services: Services.Binder) {
        val screen: BaseScreen<Any?>
        if (services.getKey<Any>() is BaseScreen<*>) {
            screen = services.getKey()
        } else {
            throw IllegalStateException()
        }

        val screenComponent: Any
        if (screen is TreeKey) {
            val parentScreen = screen.parentKey as BaseScreen<*>
            val parentScopeName = parentScreen.scopeName

            screenComponent = screen.createScreenComponent(services.getService<Any>(parentScopeName))
        } else {
            screenComponent = screen.createScreenComponent(DaggerService.rootActivityComponent)
        }

        val scopeName = screen.scopeName
        services.bind(scopeName, screenComponent)
    }

    override fun tearDownServices(services: Services) {
        super.tearDownServices(services)
    }
}

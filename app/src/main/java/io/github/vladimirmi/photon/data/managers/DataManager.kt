package io.github.vladimirmi.photon.data.managers

import io.github.vladimirmi.photon.core.App
import io.github.vladimirmi.photon.data.network.api.RestService
import io.github.vladimirmi.photon.di.DaggerScope
import javax.inject.Inject

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

@DaggerScope(App::class)
class DataManager
@Inject
constructor(private val mRestService: RestService,
            private val mPreferencesManager: PreferencesManager)

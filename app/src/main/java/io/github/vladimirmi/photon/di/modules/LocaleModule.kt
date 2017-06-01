package io.github.vladimirmi.photon.di.modules

import android.content.Context

import dagger.Module
import dagger.Provides
import io.github.vladimirmi.photon.core.App
import io.github.vladimirmi.photon.data.managers.PreferencesManager
import io.github.vladimirmi.photon.di.DaggerScope

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

@Module
class LocaleModule(internal val mContext: Context) {

    @Provides
    @DaggerScope(App::class)
    fun provideContext(): Context = mContext

    @Provides
    @DaggerScope(App::class)
    fun providePreferencesManager(context: Context): PreferencesManager = PreferencesManager(context)
}

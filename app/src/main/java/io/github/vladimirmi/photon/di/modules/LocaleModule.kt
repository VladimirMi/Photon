package io.github.vladimirmi.photon.di.modules

import android.content.Context
import com.facebook.stetho.Stetho
import com.uphyca.stetho_realm.RealmInspectorModulesProvider
import dagger.Module
import dagger.Provides
import io.github.vladimirmi.photon.core.App
import io.github.vladimirmi.photon.data.managers.PreferencesManager
import io.github.vladimirmi.photon.data.managers.RealmManager
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

    @Provides
    @DaggerScope(App::class)
    fun provideRealmManager(context: Context): RealmManager {
        Stetho.initialize(Stetho.newInitializerBuilder(context)
                .enableDumpapp(Stetho.defaultDumperPluginsProvider(context))
                .enableWebKitInspector(RealmInspectorModulesProvider.builder(context).build())
                .build())
        return RealmManager()
    }
}

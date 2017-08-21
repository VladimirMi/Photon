package io.github.vladimirmi.photon.di.modules

import android.content.Context
import com.birbit.android.jobqueue.JobManager
import com.birbit.android.jobqueue.config.Configuration
import com.facebook.stetho.Stetho
import com.uphyca.stetho_realm.RealmInspectorModulesProvider
import dagger.Module
import dagger.Provides
import io.github.vladimirmi.photon.core.App
import io.github.vladimirmi.photon.data.jobs.queue.AlbumJobQueue
import io.github.vladimirmi.photon.data.jobs.queue.JobQueue
import io.github.vladimirmi.photon.data.jobs.queue.PhotocardJobQueue
import io.github.vladimirmi.photon.data.jobs.queue.ProfileJobQueue
import io.github.vladimirmi.photon.data.managers.Cache
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.managers.PreferencesManager
import io.github.vladimirmi.photon.data.managers.RealmManager
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.utils.AppConfig

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

@Module
class LocaleModule(val context: Context) {

    @Provides
    @DaggerScope(App::class)
    fun provideContext() = context

    @Provides
    @DaggerScope(App::class)
    fun providePreferencesManager(context: Context) = PreferencesManager(context)

    @Provides
    @DaggerScope(App::class)
    fun provideCacheManager() = Cache()

    @Provides
    @DaggerScope(App::class)
    fun provideRealmManager(context: Context): RealmManager {
        Stetho.initialize(Stetho.newInitializerBuilder(context)
                .enableDumpapp(Stetho.defaultDumperPluginsProvider(context))
                .enableWebKitInspector(RealmInspectorModulesProvider.builder(context).build())
                .build())
        return RealmManager()
    }

    @Provides
    @DaggerScope(App::class)
    fun provideJobManager(context: Context): JobManager {
        val configuration = Configuration.Builder(context)
                .minConsumerCount(AppConfig.MIN_CONSUMER_COUNT)
                .maxConsumerCount(AppConfig.MAX_CONSUMER_COUNT)
                .loadFactor(AppConfig.LOAD_FACTOR)
                .consumerKeepAlive(AppConfig.CONSUMER_KEEP_ALIVE)
                .build()

        return JobManager(configuration)
    }

    @Provides
    @DaggerScope(App::class)
    fun provideJobQueue(dataManager: DataManager, jobManager: JobManager) =
            JobQueue(dataManager, jobManager)

    @Provides
    @DaggerScope(App::class)
    fun provideRefWatcher(context: Context) = App.getRefWatcher(context)

    @Provides
    @DaggerScope(App::class)
    fun providePhotocardJobQueue(jobQueue: JobQueue) = PhotocardJobQueue(jobQueue)

    @Provides
    @DaggerScope(App::class)
    fun provideAlbumJobQueue(jobQueue: JobQueue) = AlbumJobQueue(jobQueue)

    @Provides
    @DaggerScope(App::class)
    fun provideProfileJobQueue(jobQueue: JobQueue) = ProfileJobQueue(jobQueue)
}

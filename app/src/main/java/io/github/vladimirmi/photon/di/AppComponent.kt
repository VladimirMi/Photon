package io.github.vladimirmi.photon.di

import android.content.Context
import com.birbit.android.jobqueue.JobManager
import dagger.Component
import io.github.vladimirmi.photon.core.App
import io.github.vladimirmi.photon.data.repository.album.AlbumJobRepository
import io.github.vladimirmi.photon.data.repository.photocard.PhotocardJobRepository
import io.github.vladimirmi.photon.data.repository.profile.ProfileJobRepository
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
    fun context(): Context
    fun jobManager(): JobManager

    fun profileJobRepository(): ProfileJobRepository
    fun albumJobRepository(): AlbumJobRepository
    fun photocardJobRepository(): PhotocardJobRepository
}
